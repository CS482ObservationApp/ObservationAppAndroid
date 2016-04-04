package ca.zhuoliupei.observationapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

import Adapter.SearchResultAdapter;
import Adapter.SearchResultObservationEntryObject;
import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.AnimationUtil;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Model.SerializableNameValuePair;

public class SearchResultActivity extends AppCompatActivity{

    private final String NID="nid";
    private final String PAGE_INDEX="page";
    private final int MAX_RESULT_COUNT=500;// MAX_RESULT_COUNT should be integral multiple of ITEM_PER_PAGE
    private final int ITEM_PER_PAGE =10;
    private final double SPEED_THREADSHOLD=1;
    private final String JSON_IMAGE="Image";
    private final String JSON_NID="Nid";
    private final String JSON_TITLE="title";
    private final String JSON_OBSERVATION_DATE="Date Observed";
    private final String JSON_CATEGORY="Category";
    private final String JSON_RECORD="Climate Diary Record";
    private int lastLoadedPageIndex =0; //The last loaded page with full ITEM_PER_PAGE items loaded, beginning from 0

    //View control variables
    boolean userScrolled;
    private int prevFirstVisibleItem;
    private long timeStamp;
    private long toolbarTitleClickedTimeStamp=0;
    private double scrollingSpeed;
    private boolean flag_loading;

    //Download storage objects
    SearchResultAdapter searchResultAdapter;
    ArrayList<SearchResultObservationEntryObject> lvDataSet;
    ArrayList<Bitmap> imagePool;
    //Views
    ListView contentLV;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;
    RelativeLayout scrollviewFooterView;
    ImageView listViewFooterImageView;
    TextView listViewFooterTextView;
    RelativeLayout spaceHolderRL;
    TextView spaceHolderTxt;

    //Http services object
    String baseUrl,endpoint;
    DrupalServicesView drupalServicesView;
    DrupalAuthSession drupalAuthSession;
    BasicNameValuePair[] searchParams;//can only be assigned in acceptIntentExtras()

    //Asyntasks
    DownloadObservationObjectTask downloadObservationObjectTask;
    DetectPictureExistTask detectPictureExistTask;

    private enum Action{
        REFRESH,APPEND
    }

    @Override
    protected void onDestroy() {
        if (downloadObservationObjectTask!=null)
            downloadObservationObjectTask.cancel(true);
        if (detectPictureExistTask!=null)
            detectPictureExistTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        acceptIntentExtras();
        initializeVariables();
        initializeUI();

        beginDownloadObservation();
        beginDetectImage();
    }


    private void acceptIntentExtras(){
        Bundle extras = getIntent().getExtras();
        ArrayList<SerializableNameValuePair> paramList  =  (ArrayList<SerializableNameValuePair>) extras.getSerializable("params");
        searchParams=getParams(paramList);
    }
    private void initializeVariables(){
        //Init http variables
        baseUrl= getText(R.string.drupal_site_url).toString();
        endpoint=getText(R.string.drupal_server_endpoint).toString();
        drupalServicesView=new DrupalServicesView(baseUrl,endpoint);
        drupalAuthSession=new DrupalAuthSession();
        drupalAuthSession.setSession(PreferenceUtil.getCookie(this));
        drupalServicesView.setAuth(drupalAuthSession);

        //Init views references
        contentLV=(ListView)findViewById(R.id.content_lv_SearchResultActivity);
        toolbar=(Toolbar)findViewById(R.id.toolbarSearchResultActivity);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_SearchResultActivity);

        //Init view control flags
        userScrolled=false;

        //Init other variables
        lvDataSet=new ArrayList<>();
        searchResultAdapter=new SearchResultAdapter(lvDataSet,this,MAX_RESULT_COUNT);
        imagePool=new ArrayList<>();
    }
    private void initializeUI() {
        initializeContentView();
        initializeToolBar();
        setWidgetListeners();
    }


    //Contained in initialUI()
    private void initializeContentView() {
        showRefreshView();
        contentLV.setAdapter(searchResultAdapter);
    }
    private void initializeToolBar(){
        ToolBarStyler.styleToolBar(this, toolbar, getString(R.string.my_post_toolbar_title_searchResultActivity));
    }
    private void setWidgetListeners() {
        setFloatingButtonOnClick();
        setContentLVOnItemClick();
        setContentLVOnScroll();
        setSwipeRefreshOnRefresh();
        setToolbarOnClick();
    }

    //Contained in setWidgetListeners()
    private void setFloatingButtonOnClick() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_SearchResultActivity);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(SearchResultActivity.this, UploadActivity.class));
                }
            });
        }
    }
    private void setContentLVOnItemClick() {
        contentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                synchronized (lvDataSet) {
                    //If the footer item is clicked,do nothing
                    if (position >= lvDataSet.size())
                        return;

                    SearchResultObservationEntryObject selectedObject = lvDataSet.get(position);
                    Intent intent = new Intent(SearchResultActivity.this, ObservationDetailActivity.class);
                    intent.putExtra(NID, selectedObject.nid);
                    startActivity(intent);
                }
            }
        });
    }
    private void setContentLVOnScroll() {
        // Load data and append to Gridview when scroll to bottom
        contentLV.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /*Make sure that the user touched and scrolled
                * See: http://stackoverflow.com/questions/16073368/onscroll-gets-called-when-i-set-listview-onscrolllistenerthis-but-without-any
                */
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                } else {
                    userScrolled = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Calculate the scroll speed
                if (prevFirstVisibleItem != firstVisibleItem) {
                    long currTime = System.currentTimeMillis();
                    long timeToScrollOneElement = currTime - timeStamp;
                    scrollingSpeed = ((double) 1 / timeToScrollOneElement) * 1000;
                    prevFirstVisibleItem = firstVisibleItem;
                    timeStamp = currTime;
                } else {
                    scrollingSpeed = 0;
                }


                //totalItemCount includes footer
                if (userScrolled  && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (totalItemCount - 1 < MAX_RESULT_COUNT) {
                    /* If the user scrolled to bottom back and forth many times
                     * Don't start a new task to download new data until the old one is done
                     * If there's an old task running, the flag_loading is true;
                    */
                        if (!flag_loading) {
                            showLoadingMoreView();
                            downloadObservationObjectTask = new DownloadObservationObjectTask();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Action.APPEND);
                            } else {
                                downloadObservationObjectTask.execute(Action.APPEND);
                            }
                            flag_loading = true;
                        }
                    }else {
                        showLoadMoreView();
                    }
                }
            }
        });
    }
    private void setSwipeRefreshOnRefresh(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                beginDownloadObservation();
            }
        });
    }
    private void setToolbarOnClick(){
        if (toolbar==null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar_NewestActivity);
        }
        if (toolbar!=null) {
            //Double clicke the tool bar to scroll to top and refresh
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // How much time passed since last click
                    long currentTimeInMillSecond = System.currentTimeMillis();
                    double timePassed = (currentTimeInMillSecond - toolbarTitleClickedTimeStamp) / 1000;
                    if (timePassed < 0.3) {
                        scrollListViewToTop();
                        showRefreshView();
                        beginDownloadObservation();
                    }
                    toolbarTitleClickedTimeStamp = currentTimeInMillSecond;
                }
            });
        }
    }




    private void scrollListViewToTop(){
        if (contentLV!=null)
            contentLV.smoothScrollToPosition(0);
    }
    private void showRefreshView() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);

            }
        });
    }
    private void hideRefreshView() {
        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }
    private void showNoResultView(){
        if (spaceHolderTxt==null)
            spaceHolderTxt=(TextView)findViewById(R.id.txtSpaceHolder_SearchResultActivity);
        if (swipeRefreshLayout==null)
            swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_SearchResultActivity);
        if (spaceHolderRL==null)
            spaceHolderRL=(RelativeLayout)findViewById(R.id.spaceHolder_SearchResultActivity);

        if (spaceHolderRL!=null)
            spaceHolderRL.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout!=null)
            swipeRefreshLayout.setVisibility(View.GONE);
        if (spaceHolderTxt!=null) {
            spaceHolderTxt.setText(R.string.no_search_result_searchResultActivity);
            spaceHolderTxt.setVisibility(View.VISIBLE);
        }
    }
    private void hideNoResultView(){
        if (spaceHolderTxt==null)
            spaceHolderTxt=(TextView)findViewById(R.id.txtSpaceHolder_SearchResultActivity);
        if (swipeRefreshLayout==null)
            swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_SearchResultActivity);
        if (spaceHolderRL==null)
            spaceHolderRL=(RelativeLayout)findViewById(R.id.spaceHolder_SearchResultActivity);

        if (spaceHolderRL!=null)
            spaceHolderRL.setVisibility(View.GONE);
        if (swipeRefreshLayout!=null)
            swipeRefreshLayout.setVisibility(View.VISIBLE);
    }
    private void showLoadingMoreView() {
        if (scrollviewFooterView == null)
            scrollviewFooterView=(RelativeLayout)findViewById(R.id.scrollview_footer);
        if (listViewFooterImageView == null)
            listViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (listViewFooterTextView == null)
            listViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if ( scrollviewFooterView !=null){
            if (scrollviewFooterView.getVisibility()!=View.VISIBLE)
                scrollviewFooterView.setVisibility(View.VISIBLE);
            scrollviewFooterView.setOnClickListener(null);
        }
        if (listViewFooterImageView != null) {
            if (listViewFooterImageView.getVisibility() != View.VISIBLE)
                listViewFooterImageView.setVisibility(View.VISIBLE);
            if (listViewFooterImageView.getAnimation() == null) {
                RotateAnimation rotateAnimation = AnimationUtil.getRotateAnimation();
                listViewFooterImageView.startAnimation(rotateAnimation);
            }
        }
        if (listViewFooterTextView != null) {
            if (listViewFooterTextView.getVisibility() != View.VISIBLE)
                listViewFooterTextView.setVisibility(View.VISIBLE);
            listViewFooterTextView.setText(R.string.loading_newestObservationActivity);
        }
    }
    private void showNoMoreObservationView() {
        if (scrollviewFooterView == null)
            scrollviewFooterView=(RelativeLayout)findViewById(R.id.scrollview_footer);
        if (listViewFooterImageView == null)
            listViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (listViewFooterTextView == null)
            listViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if ( scrollviewFooterView !=null){
            if (scrollviewFooterView.getVisibility()!=View.VISIBLE)
                scrollviewFooterView.setVisibility(View.VISIBLE);
            scrollviewFooterView.setOnClickListener(null);
        }
        if (listViewFooterImageView != null) {
            if (listViewFooterImageView.getAnimation()!=null)
                listViewFooterImageView.setAnimation(null);
            if (listViewFooterImageView.getVisibility()!=View.GONE)
                listViewFooterImageView.setVisibility(View.GONE);
        }
        if (listViewFooterTextView != null) {
            listViewFooterTextView.setText(R.string.no_more_searchResultActivity);
        }
    }
    private void showLoadMoreView(){
        if (scrollviewFooterView == null)
            scrollviewFooterView=(RelativeLayout)findViewById(R.id.scrollview_footer);
        if (listViewFooterImageView == null)
            listViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (listViewFooterTextView == null)
            listViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if ( scrollviewFooterView !=null){
            if (scrollviewFooterView.getVisibility()!=View.VISIBLE)
                scrollviewFooterView.setVisibility(View.VISIBLE);
            scrollviewFooterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadNextPage();
                }
            });
        }
        if (listViewFooterImageView != null) {
            if (listViewFooterImageView.getAnimation()!=null)
                listViewFooterImageView.setAnimation(null);
            if (listViewFooterImageView.getVisibility()!=View.GONE)
                listViewFooterImageView.setVisibility(View.GONE);
        }
        if (listViewFooterTextView != null) {
            listViewFooterTextView.setText(R.string.click_to_load_more_searchResultActivity);
        }

    }
    public void loadNextPage() {
        synchronized (lvDataSet) {
            lvDataSet.clear();
            searchResultAdapter.notifyDataSetChanged();
            showRefreshView();
        }
        /**If the activity is downloading something, it must be refreshing
         * Because inside listview onscroll, if the totalItemSize is >=  Max
         * it won't start another task to load more.
         * Since refresh has higher priority, don't do anything if it's refreshing
         * If it's not refreshing, start to download more
         * */
        if (!flag_loading){
            downloadObservationObjectTask=new DownloadObservationObjectTask();
            downloadObservationObjectTask.execute(Action.APPEND);
        }
    }

    private class DownloadObservationObjectTask extends AsyncTask<Action,Void, SearchResultObservationEntryObject[]>{
        Action action;
        @Override
        protected SearchResultObservationEntryObject[] doInBackground(Action... actions) {
            try {
                action=actions[0];
                BasicNameValuePair[] params=new BasicNameValuePair[searchParams.length+1];
                for (int i=0;i<searchParams.length;i++)
                    params[i]=searchParams[i];

                if (action==Action.REFRESH) {
                    lastLoadedPageIndex = 0;
                    params[searchParams.length] = new BasicNameValuePair(PAGE_INDEX, String.valueOf(lastLoadedPageIndex));
                }else {
                    params[searchParams.length]=new BasicNameValuePair(PAGE_INDEX, String.valueOf(lastLoadedPageIndex +1));
                }
                HashMap<String,String> resultMap = drupalServicesView.retrieve(DrupalServicesView.ViewType.OBSERVATION_SEARCH, params);
                return getObservationObjectsFromResponse(resultMap);
            }catch (Exception ex){
                return new SearchResultObservationEntryObject[0];
            }
        }

        @Override
        protected void onPostExecute(SearchResultObservationEntryObject[] downloadedObjects) {
            hideRefreshView();
            /**If refresh, it would clear all existing result
             * We need to consider when the count of object we downloaded is :
             * 1. Zero, no object downloaded, maybe network error, then show no result view
             * 2. NonZero but less than ITEM_PER_PAGE, then show no more result view
             * 3. Equals to ITEM_PER_PAGE is downloaded, then do nothing
             *
             * If append, add the downloaded objects to data set if it's not existing
             * If the downloaded objects count is less than ITEM_PER_PAGE, then show no result view
             * If the size of dataset exceeded the maximum, show LoadMore view,
             * when user click the footer it would load more but clear the dataset first**/
            synchronized (lvDataSet) {
                if (action == Action.REFRESH) {
                    if (downloadedObjects.length == 0) {
                        showNoResultView();
                    } else {
                        lvDataSet.clear();
                        lastLoadedPageIndex = 0;
                        for (SearchResultObservationEntryObject downloadedObject : downloadedObjects) {
                            lvDataSet.add(downloadedObject);
                        }
                        searchResultAdapter.notifyDataSetChanged();
                        hideNoResultView();
                        if (lvDataSet.size() < ITEM_PER_PAGE) {
                            showNoMoreObservationView();
                        }
                    }
                } else {
                    hideNoResultView();
                    //Checked if object already exist in the original dataset
                    for (SearchResultObservationEntryObject downloadedObject : downloadedObjects) {
                        boolean objectExist = false;
                        for (SearchResultObservationEntryObject dataSetObject : lvDataSet) {
                            if (downloadedObject.nid.equals(dataSetObject.nid)) {
                                objectExist = true;
                                break;
                            }
                        }
                        if (!objectExist&&lvDataSet.size()<MAX_RESULT_COUNT)
                            lvDataSet.add(downloadedObject);
                    }
                    searchResultAdapter.notifyDataSetChanged();
                    boolean fullDataSet=lvDataSet.size()>=MAX_RESULT_COUNT;
                    boolean endOfResult=downloadedObjects.length < ITEM_PER_PAGE;

                    if (endOfResult)
                        showNoMoreObservationView();
                    else
                        lastLoadedPageIndex++;

                    if (fullDataSet)
                        showLoadMoreView();
                }
                flag_loading = false;
            }
        }
    }
    private class DetectPictureExistTask extends AsyncTask<Void, Void, Void>{
        ArrayList<SearchResultObservationEntryObject> visibleObjects;
        ArrayList<SearchResultObservationEntryObject> objectsLackPicture;
        Context context;
        public DetectPictureExistTask(Context context) {
            this.visibleObjects = new ArrayList<>();
            this.objectsLackPicture = new ArrayList<>();
            this.context=context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!this.isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("DETECT_LACK_PICTURE", "Detect Picture Task Sleep interrupted");
                }
                //If the user scroll too fast, don't download until it slows down
                if (scrollingSpeed > SPEED_THREADSHOLD)
                    continue;
                 /* Lock the listview dataset
                 * Get all visible objects */
                synchronized (lvDataSet) {
                    visibleObjects.clear();
                    if (searchResultAdapter != null&&lvDataSet.size()>0) {
                        //TODO: Getting UI element would not matter, but better to change to use Handler
                        int firstVisiblePosition = contentLV.getFirstVisiblePosition();
                        int lastVisiblePosition = contentLV.getLastVisiblePosition();
                        if (lastVisiblePosition >= lvDataSet.size())
                            lastVisiblePosition = lvDataSet.size() - 1;
                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++)
                            visibleObjects.add(lvDataSet.get(i));
                    }
                }

                //Get all visible objects that don't have cached picture
                objectsLackPicture.clear();
                for (SearchResultObservationEntryObject object : visibleObjects) {
                    if (object.imgaeBitmap==null)
                        objectsLackPicture.add(object);
                }

                //Download the image and store it into bitmap pool
                for (SearchResultObservationEntryObject object : objectsLackPicture) {
                    //Get the uri of the object's image
                    String serverUri = object.imageUrl;
                    if (serverUri == null || serverUri.isEmpty())
                        continue;
                    Bitmap image= PhotoUtil.getBitmapFromServerURL(serverUri);
                    if (image!=null) {
                        object.imgaeBitmap = image;
                        imagePool.add(image);
                        publishProgress();
                    }
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            if (searchResultAdapter != null)
                searchResultAdapter.notifyDataSetChanged();
        }
    }


    private void beginDownloadObservation() {
        if (downloadObservationObjectTask!=null)
            downloadObservationObjectTask.cancel(true);
        flag_loading=true;
        downloadObservationObjectTask = new DownloadObservationObjectTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Action.REFRESH);
        } else {
            downloadObservationObjectTask.execute(Action.REFRESH);
        }
    }
    private void beginDetectImage(){
        detectPictureExistTask=new DetectPictureExistTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            detectPictureExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            detectPictureExistTask.execute();
        }
    }

    /******Helper Methods*******/

    private BasicNameValuePair[] getParams(ArrayList<SerializableNameValuePair> paramList){
        BasicNameValuePair[] params=new BasicNameValuePair[paramList.size()];
        for (int i=0;i<paramList.size();i++){
            params[i]=new BasicNameValuePair(paramList.get(i).getName(),paramList.get(i).getValue());
        }
        return params;
    }
    private SearchResultObservationEntryObject[] getObservationObjectsFromResponse(HashMap<String, String> responseMap) {
        // Fetch info from json response, store the image url and other info into each observation objects
        if (responseMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
            try {
                JSONArray responseJsonArray = new JSONArray(responseMap.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
                SearchResultObservationEntryObject[] observationEntryObjects = new SearchResultObservationEntryObject[responseJsonArray.length()];
                for (int i = 0; i < responseJsonArray.length(); i++) {
                    JSONObject observationJsonObject = responseJsonArray.getJSONObject(i);

                    String photoUri = "";
                    try {
                        String imgStr=observationJsonObject.getString(JSON_IMAGE);
                        Document photoHtml = Jsoup.parseBodyFragment(imgStr);
                        photoUri = photoHtml.body().getElementsByTag("img").attr("src");
                    } catch (Exception e) {
                    }
                    String title = observationJsonObject.getString(JSON_TITLE);
                    String date = observationJsonObject.getString(JSON_OBSERVATION_DATE).split("[ ]")[0];
                    String nid = observationJsonObject.getString(JSON_NID);
                    String category = observationJsonObject.getString(JSON_CATEGORY);
                    String record = observationJsonObject.getString(JSON_RECORD);

                    SearchResultObservationEntryObject object = new SearchResultObservationEntryObject();
                    object.title = title;
                    object.date = date;
                    object.nid = nid;
                    object.category=category;
                    object.record=record;
                    object.imageUrl=photoUri;
                    observationEntryObjects[i] = object;
                }
                return observationEntryObjects;
            } catch (Exception ex) {
                return new SearchResultObservationEntryObject[0];
            }
        } else {
            return new SearchResultObservationEntryObject[0];
        }
}}

