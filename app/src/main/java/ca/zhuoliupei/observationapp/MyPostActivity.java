package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import Adapter.MyObservationAdapter;
import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import DBHelper.NewestObservationDBHelper;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.AnimationUtil;
import HelperClass.DownLoadUtil;
import HelperClass.MyObservationCacheManager;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Model.ObservationEntryObject;

public class MyPostActivity extends AppCompatActivity {
    private final static String NID = "nid";
    private final static String DATE = "date";
    private final static String ACTION = "action";
    private final static String COOKIE = "cookie";
    private final static String JSON_TITLE = "title";
    private final static String JSON_OBSERVATION_DATE = "Date Observed";
    private final static String JSON_IMAGE = "Image";
    private final static String JSON_NID = "Nid";

    private enum Action {
        REFRESH, APPEND
    }

    private final ArrayList<ObservationEntryObject> lvDataSet = new ArrayList<>();
    private  MyObservationAdapter myObservationAdapter;
    private  MyObservationCacheManager myObservationCacheManager;

    private Toolbar toolbar;

    private String userName;
    private String cookie;
    private final double speedThreadshold = 1;
    private int loadedPage = 0;
    private int itemsPerPage = 10;
    private int prevFirstVisibleItem;
    private long timeStamp;
    private long toolbarTitleClickedTimeStamp=0;
    private double scrollingSpeed;
    private boolean userScrolled;
    private boolean flag_loading;


    private DetectPictureExistTask detectPictureExistTask;
    private DownloadObservationObjectTask downloadObservationObjectTask;
    private CacheObservationObjectTask cacheObservationObjectTask;
    private RefreshContentViewDataSetTask refreshContentViewDatasetTask;

    private ListView contentLV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView listViewFooterImageView;
    private TextView listViewFooterTextView;
    private RelativeLayout scrollviewFooterView;
    private TextView spaceHolderTxt;


    @Override
    protected void onDestroy() {
        if (downloadObservationObjectTask != null)
            downloadObservationObjectTask.cancel(true);
        if (detectPictureExistTask != null)
            detectPictureExistTask.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        initializeVariables();
        initializeUI();
        setWidgetListeners();

        beginDownloadObservation();
        beginDetectPicture();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Contained in onCreate()
    private void initializeVariables() {
        flag_loading = false;
        userScrolled=false;
        contentLV = (ListView) findViewById(R.id.content_lv_MyPostActivity);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_MyPostActivity);
        toolbar = (Toolbar) findViewById(R.id.toolbar_MyPostActivity);

        myObservationAdapter = new MyObservationAdapter(lvDataSet, this);
        myObservationCacheManager = MyObservationCacheManager.getInstance(this);
        userName = PreferenceUtil.getCurrentUser(this);
        cookie=PreferenceUtil.getCookie(this);
    }
    private void initializeUI() {
        initializeContentView();
        initializeToolBar();
    }
    private void setWidgetListeners() {
        setFloatingButtonOnClick();
        setContentLVOnItemClick();
        setContentLVOnScroll();
        setSwipeRefreshOnRefresh();
        setToolbarOnClick();
    }
    private void beginDownloadObservation() {
        HashMap<String, String> sessionInfoMap = new HashMap<>();
        sessionInfoMap.put(COOKIE, cookie);
        sessionInfoMap.put(ACTION, Action.REFRESH.toString());
        downloadObservationObjectTask = new DownloadObservationObjectTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
        } else {
            downloadObservationObjectTask.execute(sessionInfoMap);
        }
    }
    private void beginDetectPicture() {
        HashMap<String, String> sessionInfoMap = new HashMap<>();
        sessionInfoMap.put(COOKIE, cookie);
        sessionInfoMap.put(ACTION, Action.REFRESH.toString());
        detectPictureExistTask = new DetectPictureExistTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            detectPictureExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
        } else {
            detectPictureExistTask.execute(sessionInfoMap);
        }
    }

    //Contained in initialUI()
    private void initializeContentView() {
        showRefreshView();
        contentLV.setAdapter(myObservationAdapter);
    }
    private void initializeToolBar(){
        ToolBarStyler.styleToolBar(this, toolbar, getString(R.string.my_post_toolbar_title_myPostActivity));
    }

    //Contained in setWidgetListeners()
    private void setFloatingButtonOnClick() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_MyPostActivity);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MyPostActivity.this, UploadActivity.class));
                }
            });
        }
    }

    private void setContentLVOnItemClick() {
        contentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                synchronized (lvDataSet) {
                    if (position == 0)
                        return; /*Do nothing when header item is clicked*/
                    if (position >= (1 + lvDataSet.size()))
                        return;/*Do nothing when footer item is clicked*/
                    ObservationEntryObject selectedObject = lvDataSet.get(position - 1);
                    Intent intent = new Intent(MyPostActivity.this, ObservationDetailActivity.class);
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
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {
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


                if (userScrolled && totalItemCount < myObservationCacheManager.getMaxCacheAmount() && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    /* If the user scrolled to bottom back and forth many times
                     * Don't start a new task to download new data until the old one is done
                     * If there's an old task running, the flag_loading is true;
                    */
                    if (!flag_loading) {
                        showLoadingMoreView();
                        HashMap<String, String> sessionInfoMap = new HashMap<>();
                        sessionInfoMap.put(DrupalServicesFieldKeysConst.LOGIN_COOKIE, cookie);
                        sessionInfoMap.put(ACTION, Action.APPEND.toString());
                        ObservationEntryObject lastobject = null;
                        synchronized (lvDataSet) {
                            if (lvDataSet.size() > 0)
                                lastobject = lvDataSet.get(lvDataSet.size() - 1);
                        }
                        if (lastobject != null) {
                            sessionInfoMap.put(DATE, lastobject.date);
                            downloadObservationObjectTask = new DownloadObservationObjectTask((Activity) view.getContext());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
                            } else {
                                downloadObservationObjectTask.execute(sessionInfoMap);
                            }
                            flag_loading = true;
                        }
                    }
                } else if (totalItemCount >= myObservationCacheManager.getMaxCacheAmount()) {
                    showSeeMoreOnWebView();
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
            //Double click the tool bar to scroll to top and refresh
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

    private class DownloadObservationObjectTask extends AsyncTask<HashMap<String, String>, Void, ObservationEntryObject[]> {
        Activity context;
        String action;

        public DownloadObservationObjectTask(Activity context) {
            this.context = context;
        }

        @Override
        protected ObservationEntryObject[] doInBackground(HashMap<String, String>... params) {
            DrupalAuthSession authSession = new DrupalAuthSession();
            DrupalServicesView myObservationView = new DrupalServicesView(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            action = params[0].get(ACTION);

            //Get cookie
            String cookie = params[0].get(COOKIE);
            authSession.setSession(cookie);
            myObservationView.setAuth(authSession);
            //Get date parameter
            String date = params[0].get(DATE);

            BasicNameValuePair[] pairs = getParams(date);
            try {
                HashMap<String, String> responseMap = myObservationView.retrieve(DrupalServicesView.ViewType.PERSONAL_OBSERVATION, pairs);
                return getObservationObjectsFromResponse(responseMap);
            } catch (Exception e) {
                return new ObservationEntryObject[0];
            }
        }

        @Override
        protected void onPostExecute(ObservationEntryObject[] observationEntryObjects) {
            //If download error, only show network unavailable when user wants to refresh
            if (observationEntryObjects.length == 0 && action.equals(Action.REFRESH.toString()))
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();

            cacheObservationObjectTask = new CacheObservationObjectTask(context, action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                cacheObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, observationEntryObjects);
            } else {
                cacheObservationObjectTask.execute(observationEntryObjects);
            }
        }
    }
    private class CacheObservationObjectTask extends AsyncTask<ObservationEntryObject[], Void, Void> {
        Activity context;
        String action;

        public CacheObservationObjectTask(Activity context, String action) {
            this.context = context;
            this.action = action;
        }

        @Override
        protected Void doInBackground(ObservationEntryObject[]... params) {
            //Cache to database
            ObservationEntryObject[] observationEntryObjects = params[0];
            if (observationEntryObjects.length > 0) {
                myObservationCacheManager.cache(observationEntryObjects);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshContentViewDatasetTask = new RefreshContentViewDataSetTask(context, action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                refreshContentViewDatasetTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                refreshContentViewDatasetTask.execute();
            }
        }
    }
    private class RefreshContentViewDataSetTask extends AsyncTask<Void, Void, ArrayList<ObservationEntryObject>> {
        Activity context;
        String action;
        boolean itemAmountChanged;

        public RefreshContentViewDataSetTask(Activity context, String action) {
            this.context = context;
            this.action = action;
        }

        @Override
        protected ArrayList<ObservationEntryObject> doInBackground(Void... params) {
            if (action.equals(Action.REFRESH.toString())) {
                loadedPage = 0;
            }
            ArrayList<ObservationEntryObject> newList;
            newList = myObservationCacheManager.getCache((loadedPage + 1) * itemsPerPage);
            return newList;
        }

        @Override
        protected void onPostExecute(ArrayList<ObservationEntryObject> newList) {
            if (contentLV == null)
                return;

            synchronized (lvDataSet) {
                itemAmountChanged = (newList.size() != lvDataSet.size());
                if (itemAmountChanged || action.equals(Action.REFRESH.toString())) {
                    lvDataSet.clear();
                    for (ObservationEntryObject object : newList) {
                        lvDataSet.add(object);
                    }
                    loadedPage++;
                    myObservationAdapter.notifyDataSetChanged();
                }

                if (contentLV.getVisibility() != View.VISIBLE)
                    contentLV.setVisibility(View.VISIBLE);
                if (lvDataSet.size() <= 0)
                    showNoPostView();
                else if (action.equals(Action.REFRESH.toString()))
                    scrollListViewToTop();
                else if (action.equals(Action.APPEND.toString())&&!itemAmountChanged)
                    showNoMoreObservationView();
                else if (action.equals(Action.APPEND.toString())&&lvDataSet.size()>=myObservationCacheManager.getMaxCacheAmount())
                    showSeeMoreOnWebView();
                flag_loading = false;
                hideRefreshView();
            }
        }
    }


    /* This task runs in the back ground until the activity destroyed
    *  It scans through the visible items in GridView to see if the local picture file exits
    *  If not, download the file, and update the location in the database,refresh Gridview adapter*/
    private class DetectPictureExistTask extends AsyncTask<HashMap<String, String>, Void, Void> {
        Activity context;
        ArrayList<ObservationEntryObject> visibleObjects;
        ArrayList<ObservationEntryObject> objectsLackPicture;
        NewestObservationDBHelper dbHelper;
        SQLiteDatabase writableDatabase;
        SQLiteDatabase readableDatabase;
        Cursor c = null;


        public DetectPictureExistTask(Activity context) {
            this.context = context;
            this.visibleObjects = new ArrayList<>();
            this.objectsLackPicture = new ArrayList<>();
            this.dbHelper = new NewestObservationDBHelper(context);
            this.writableDatabase = dbHelper.getWritableDatabase();
            this.readableDatabase = dbHelper.getReadableDatabase();
        }

        @Override
        protected void onCancelled() {
            if (c != null) {
                c.close();
            }
            readableDatabase.close();
            writableDatabase.close();
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(HashMap<String, String>... params) {
            while (!this.isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("DETECT_LACK_PICTURE", "Detect Picture Task Sleep interrupted");
                }
                //If the user scroll too fast, don't download until it slows down
                if (scrollingSpeed > speedThreadshold)
                    continue;

                /* Lock the gridview dataset and Get all visible objects
                 * Take header, footer view into consideration
                 * So need to calculate the first and last visible position*/
                synchronized (lvDataSet) {
                    visibleObjects.clear();
                    if (myObservationAdapter != null && lvDataSet.size()>0) {
                        //TODO: Getting UI element would not matter, but better to change to use Handler
                        int firstVisiblePosition = contentLV.getFirstVisiblePosition();
                        int lastVisiblePosition = contentLV.getLastVisiblePosition();
                        if (firstVisiblePosition<=0)
                            firstVisiblePosition=1;
                        if (lastVisiblePosition > lvDataSet.size())
                            lastVisiblePosition = lvDataSet.size();
                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++)
                            visibleObjects.add(lvDataSet.get(i-1));
                    }
                }

                //Get all visible objects that don't have cached picture
                objectsLackPicture.clear();
                for (ObservationEntryObject object : visibleObjects) {
                    if (object.photoLocalUri == null || object.photoLocalUri.isEmpty() || !new File(object.photoLocalUri).exists())
                        objectsLackPicture.add(object);
                }

                //Download the image to local first
                for (ObservationEntryObject object : objectsLackPicture) {
                    //Get the uri of the object's image
                    String serverUri = object.photoServerUri;
                    if (serverUri == null || serverUri.isEmpty())
                        continue;
                    String nid = object.nid;
                    String filename = new File(context.getFilesDir(), System.currentTimeMillis() + nid).getPath();

                    //DOWNLOAD IMAGE TO LOCAL
                    boolean downloadSucceed = DownLoadUtil.downloadImage(serverUri, filename);
                    if (downloadSucceed) {
                        object.photoLocalUri = filename;
                        publishProgress();
                    }
                }
                //Update Database( if the record is deleted from DB, the image would also be deleted in updateRecordImageLocation(objectsLackPicture))
                myObservationCacheManager.updateRecordImageLocation(objectsLackPicture);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            if (myObservationAdapter != null)
                myObservationAdapter.notifyDataSetChanged();
        }
    }


    //Helper methods
    private BasicNameValuePair[] getParams(String dateTime) {
        if (dateTime != null && !dateTime.isEmpty()) {
            String date = dateTime.split(" ")[0];
            String year = date.split("-")[0];
            String month = date.split("-")[1];
            String day = date.split("-")[2];
            year = String.valueOf(Integer.parseInt(year));
            month = String.valueOf(Integer.parseInt(month));
            day = String.valueOf(Integer.parseInt(day));

            BasicNameValuePair[] returnPairs = new BasicNameValuePair[4];
            returnPairs[0] = new BasicNameValuePair("field_date_observed_value[value][year]", year);
            returnPairs[1] = new BasicNameValuePair("field_date_observed_value[value][month]", month);
            returnPairs[2] = new BasicNameValuePair("field_date_observed_value[value][day]", day);
            returnPairs[3] = new BasicNameValuePair("uid", userName);

            return returnPairs;
        }else {
            BasicNameValuePair[] returnPairs = new BasicNameValuePair[1];
            returnPairs[0] = new BasicNameValuePair("uid", userName);
            return returnPairs;
        }
    }
    private ObservationEntryObject[] getObservationObjectsFromResponse(HashMap<String, String> responseMap) {
        // Fetch info from json response, store the image url and other info into each observation objects
        if (responseMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
            try {
                JSONArray responseJsonArray = new JSONArray(responseMap.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
                ObservationEntryObject[] observationEntryObjects = new ObservationEntryObject[responseJsonArray.length()];
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
                    String date = observationJsonObject.getString(JSON_OBSERVATION_DATE);
                    String nid = observationJsonObject.getString(JSON_NID);

                    ObservationEntryObject object = new ObservationEntryObject();
                    object.title = title;
                    object.photoServerUri = photoUri;
                    object.date = date;
                    object.nid = nid;
                    observationEntryObjects[i] = object;
                }
                return observationEntryObjects;
            } catch (Exception ex) {
                return new ObservationEntryObject[0];
            }
        } else {
            return new ObservationEntryObject[0];
        }
    }
    private void showSeeMoreOnWebView() {
        if (listViewFooterImageView == null)
            listViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (listViewFooterTextView == null)
            listViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if (listViewFooterImageView != null) {
            listViewFooterImageView.setAnimation(null);
            listViewFooterImageView.setVisibility(View.GONE);
        }
        if (listViewFooterTextView != null) {
            listViewFooterTextView.setText(R.string.view_more_on_web_myPostActivity);
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
    private void showNoPostView(){
        if (spaceHolderTxt==null)
            spaceHolderTxt=(TextView)findViewById(R.id.txtSpaceHolder_MyPostHeader);
        if (contentLV==null)
            contentLV=(ListView)findViewById(R.id.content_lv_MyPostActivity);


        if (contentLV!=null)
            contentLV.setVisibility(View.GONE);
        if (spaceHolderTxt!=null) {
            spaceHolderTxt.setText(R.string.write_something_myPostActivity);
            spaceHolderTxt.setVisibility(View.VISIBLE);
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
        }
        if (listViewFooterImageView != null) {
            if (listViewFooterImageView.getAnimation()!=null)
                listViewFooterImageView.setAnimation(null);
            if (listViewFooterImageView.getVisibility()!=View.GONE)
                listViewFooterImageView.setVisibility(View.GONE);
        }
        if (listViewFooterTextView != null) {
            listViewFooterTextView.setText(R.string.no_more_myPostActivity);
        }
    }
}

