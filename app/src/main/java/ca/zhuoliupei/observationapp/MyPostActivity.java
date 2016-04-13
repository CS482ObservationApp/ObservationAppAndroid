/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */


package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Context;
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
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesNode;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.AnimationUtil;
import HelperClass.DownLoadUtil;
import HelperClass.MyObservationCacheManager;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Model.ObservationEntryObject;
/**This class provides functions:
 * 1. Fetch user's own posts from network show them and store them locally
 * 2. If network is not available, fetch from local database and show to user
 * 3. Detect if the cached post still exists in cloud, if not, delete it
 * 4. Provide entry to Observation Detail Activity
 * 5. Monitor when user scroll to bottom, automatically download  more
 * 6 .Control the amount of observations downloaded,stop load more when amount exceeds a max value.*/
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
    private DetectPostExistTask detectPostExistTask;

    private ListView contentLV;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView listViewFooterImageView;
    private TextView listViewFooterTextView;
    private RelativeLayout scrollviewFooterView;
    private TextView spaceHolderTxt;

    DrupalAuthSession authSession;

    @Override
    protected void onDestroy() {
//        CacheObservationTask is not canceled because it's quick
        if (downloadObservationObjectTask != null)
            downloadObservationObjectTask.cancel(true);
        if (detectPictureExistTask != null)
            detectPictureExistTask.cancel(true);
        if (refreshContentViewDatasetTask != null)
            refreshContentViewDatasetTask.cancel(true);
        if (detectPostExistTask!=null)
            detectPostExistTask.cancel(true);
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
        beginDetectPictureExist();
        beginDetectPostExist();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Wrapped in onCreate()
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

        authSession = new DrupalAuthSession();
        authSession.setSession(cookie);
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
        if (downloadObservationObjectTask!=null)
            downloadObservationObjectTask.cancel(true);
        downloadObservationObjectTask = new DownloadObservationObjectTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Action.REFRESH);
        } else {
            downloadObservationObjectTask.execute(Action.REFRESH);
        }
    }
    private void beginDetectPictureExist() {
        if (detectPostExistTask!=null)
            detectPostExistTask.cancel(true);
        detectPictureExistTask = new DetectPictureExistTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            detectPictureExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            detectPictureExistTask.execute();
        }
    }
    private void beginDetectPostExist(){
        if (detectPostExistTask!=null)
            detectPostExistTask.cancel(true);
        detectPostExistTask=new DetectPostExistTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            detectPostExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            detectPostExistTask.execute();
        }
    }

    //Wrapped in initialUI()
    private void initializeContentView() {
        showRefreshView();
        contentLV.setAdapter(myObservationAdapter);
    }
    private void initializeToolBar(){
        ToolBarStyler.styleToolBar(this, toolbar, getString(R.string.my_post_toolbar_title_myPostActivity));
    }

    //Wrapped in setWidgetListeners()
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

                        downloadObservationObjectTask = new DownloadObservationObjectTask((Activity) view.getContext());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            downloadObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Action.APPEND);
                        } else {
                            downloadObservationObjectTask.execute(Action.APPEND);
                        }

                        flag_loading = true;
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
                beginDetectPostExist();
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



    /***************Asynctasks***********************/
    /* These 3 classes are called in a consecutive way:
    * DonwloadObservationObjectTask--->CacheObservationObjectTask--->RefreshContentViewDataSetTask
    * First fetch observations in Json and construct an array of Observation Objects
    * Then pass it to Cache Manager to cache them to database
    * Lastly fetch from database to refresh the gridview (text only without picture)*/
    private class DownloadObservationObjectTask extends AsyncTask<Action, Void, ObservationEntryObject[]> {
        Activity context;
        String action;

        public DownloadObservationObjectTask(Activity context) {
            this.context = context;
        }

        @Override
        protected ObservationEntryObject[] doInBackground(Action... actions) {
            DrupalServicesView myObservationView = new DrupalServicesView(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            action = actions[0].toString();

            //Get cookie
            myObservationView.setAuth(authSession);

            BasicNameValuePair[] pairs = buildHttpParams(action);
            try {
                HashMap<String, String> responseMap = myObservationView.retrieve(DrupalServicesView.ViewType.PERSONAL_OBSERVATION, pairs);
                return getObservationObjectsFromResponse(responseMap);
            } catch (Exception e) {
                return new ObservationEntryObject[0];
            }
        }

        @Override
        protected void onPostExecute(ObservationEntryObject[] observationEntryObjects) {
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
            int loadedPage;
            if (action.equals(Action.REFRESH.toString())) {
                loadedPage = 0;
            }else {
                synchronized (lvDataSet) {
                    loadedPage = lvDataSet.size() / itemsPerPage;
                }
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
    private class DetectPictureExistTask extends AsyncTask<Void, Void, Void> {
        Activity context;
        ArrayList<ObservationEntryObject> visibleObjects;
        ArrayList<ObservationEntryObject> objectsLackPicture;


        public DetectPictureExistTask(Activity context) {
            this.context = context;
            this.visibleObjects = new ArrayList<>();
            this.objectsLackPicture = new ArrayList<>();
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

    /*This task runs once when the activity is created until it's destroyed, it scanned through all the cached post in DB
    * It sends a http request for the node to see if the node exist
    * If not,delete it from Local DB*/
    private class DetectPostExistTask extends AsyncTask<Void,Void,Void> {
        Context context;

        public DetectPostExistTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (myObservationCacheManager==null)
                myObservationCacheManager=MyObservationCacheManager.getInstance(context);
            ArrayList<ObservationEntryObject> cachedObjects = myObservationCacheManager.getCache(myObservationCacheManager.getMaxCacheAmount());
            DrupalServicesNode drupalServicesNode=new DrupalServicesNode(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());
            drupalServicesNode.setAuth(authSession);

            for (ObservationEntryObject object:cachedObjects) {
                String nid=object.nid;
                try{
                    HashMap<String,String> map = drupalServicesNode.retrieve(Integer.parseInt(nid));
                    String responseCode=map.get(DrupalServicesFieldKeysConst.STATUS_CODE);
                    if (!responseCode.equals(HTTPConst.HTTP_OK_200))
                        myObservationCacheManager.deleteCache(nid);
                }catch (Exception ex){}
            }

            return null;
        }
    }




    /**************Helper methods****************/
    private BasicNameValuePair[] buildHttpParams(String action) {
        int loadedPage;
        BasicNameValuePair[] returnPairs = new BasicNameValuePair[2];
        if (action.equals(Action.REFRESH.toString())) {
            returnPairs[0] = new BasicNameValuePair("page", String.valueOf(0));
        }
        else {
            synchronized (lvDataSet) {
                loadedPage = lvDataSet.size() / itemsPerPage;
                returnPairs[0] = new BasicNameValuePair("page", String.valueOf(loadedPage+1));
            }
        }
        returnPairs[1] = new BasicNameValuePair("uid", userName);

        return returnPairs;
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

