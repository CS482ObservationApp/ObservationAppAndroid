package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.Iterator;

import Adapter.NewestObservationAdapter;
import Adapter.SlidingMenuAdapter;
import Const.AppConst;
import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import Const.ObservationModelConst;
import Const.SharedPreferencesConst;
import DBHelper.NewestObservationDBHelper;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.AnimationUtil;
import HelperClass.DownLoadUtil;
import HelperClass.NewestObservationCacheManager;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Model.ObservationEntryObject;
import Model.SlidingMenuItem;
import ViewAndFragmentClass.GridViewWithHeaderAndFooter;

public class NewestObservationsActivity extends AppCompatActivity{

    private final static String NID = "nid";
    private final static String COOKIE = "cookie";
    private final static String ACTION = "Action";
    private final static String DATE = "date";

    private enum Action {
        REFRESH, APPEND
    }

    private int loadedPage = 0;
    private int itemsPerPage = 10;
    private String cacheFolder;
    private final ArrayList<ObservationEntryObject> gvDataset = new ArrayList<>();
    private  String cookie ;
    private boolean userScrolled = false;
    private boolean flag_loading = false;

    private DetectPictureExistTask detectPictureExistTask;
    private DownloadObservationObjectTask downloadObservationObjectTask;
    private CacheObservationObjectTask cacheObservationObjectTask;
    private RefreshGVDatasetTask refreshGVDatasetTask;
    private ValidateSessionTask validateSessionTask;

    private SwipeRefreshLayout swipeRefreshLayout;
    private GridViewWithHeaderAndFooter contentGV;
    private Toolbar toolbar;
    private ImageView gridViewFooterImageView;
    private TextView gridViewFooterTextView;

    private  SlidingMenuAdapter slidingMenuAdapter;
    private  NewestObservationAdapter contentGVAdapter;
    private  NewestObservationCacheManager newestObservationCacheManager;

    private long timeStamp;
    private long toolbarTitleClickedTimeStamp=0;
    private int prevFirstVisibleItem;
    private double scrollingSpeed;
    private final double speedThreadshold = 1;

    @Override
    public void onBackPressed() {
        //Hide the sliding menu if it's opened
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.root_dl_NewestObservationActivity);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newest_observations);
        initializeVariables();
        initializeUI();
        setWidgetListeners();

        beginValidateSession();
        beginDownloadObservation();
        beginDetectPicture();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeSlidingMenu();
        initializeFloatingButton();

    }

    private void initializeVariables() {
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swiperefresh_NewestObservationActivity));
        contentGV = (GridViewWithHeaderAndFooter) findViewById(R.id.content_gridview_NewestObsrvationActivity);
        toolbar = (Toolbar) findViewById(R.id.toolbar_NewestActivity);
        cacheFolder = getCacheDir() + "//Newest_Observation";
        cookie= PreferenceUtil.getCookie(this);
        contentGVAdapter = new NewestObservationAdapter(gvDataset, this);
        newestObservationCacheManager = NewestObservationCacheManager.getInstance(this);
    }

    private void initializeUI() {
        initializeToolBar();
        initializeContentView();
        initializeSlidingMenu();
        initializeFloatingButton();
    }

    private void setWidgetListeners() {
        setFloatingButtonOnClick();
        setGridViewOnItemClick();
        setSlidingMenuOnItemClick();
        setContentGVOnScroll();
        setToolbarNavigationOnClick();
        setSwipeRefreshOnRefresh();
        setToolbarOnClick();
    }

    //Wrapped in initializeUI()
    private void initializeContentView() {
        /*If there are observation objects contained in the Intent,
        * Show them on the GridView
        * Otherwise just hide the GridView
        * */
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra("OBSERVATION_OBJECTS");
        showRefreshView();
        addGridViewFooter();
        contentGV.setAdapter(contentGVAdapter);

        if (parcelables != null && parcelables.length > 0) {
            //Fill GridView content
            for (int i = 0; i < parcelables.length; i++) {
                gvDataset.add((ObservationEntryObject) parcelables[i]);
            }
            contentGVAdapter.notifyDataSetChanged();
        } else {
            contentGV.setVisibility(View.INVISIBLE);
        }
    }

    private void initializeSlidingMenu() {
        ListView slidingMenuList = (ListView) findViewById(R.id.sliding_menu);
        ArrayList<SlidingMenuItem> items = new ArrayList<>();

        if (PreferenceUtil.getCurrentUserStatus(this)) {
            SlidingMenuItem accountItem = new SlidingMenuItem(SlidingMenuItem.ItemType.USER_ACCOUNT_ITEM);
            items.add(accountItem);
            SlidingMenuItem uploadItem = new SlidingMenuItem(SlidingMenuItem.ItemType.UPLOAD_ITEM);
            uploadItem.text = getString(R.string.upload_slidingMenuItem);
            items.add(uploadItem);
        } else {
            SlidingMenuItem loginItem = new SlidingMenuItem(SlidingMenuItem.ItemType.LOGIN_ITEM);
            items.add(loginItem);
        }

        SlidingMenuItem searchItem = new SlidingMenuItem(SlidingMenuItem.ItemType.SEARCH_ITEM);
        searchItem.text = getString(R.string.search_slidingMenuItem);
        SlidingMenuItem userGuideItem = new SlidingMenuItem(SlidingMenuItem.ItemType.USER_GUIDE_ITEM);
        userGuideItem.text = getString(R.string.guide_slidingMenuItem);

        items.add(searchItem);
        items.add(userGuideItem);

        slidingMenuAdapter = new SlidingMenuAdapter(items, this);

        slidingMenuList.setAdapter(slidingMenuAdapter);
    }

    private void initializeToolBar() {
        ToolBarStyler.styleToolBar(this, toolbar, "Newest Observations");
    }

    private void initializeFloatingButton() {
        if (!PreferenceUtil.getCurrentUserStatus(this)) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_NewestObservationActivity);
            fab.setVisibility(View.GONE);
        }
    }


    //Wrapped in setWidgetListeners()
    private void setFloatingButtonOnClick() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_NewestObservationActivity);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(NewestObservationsActivity.this, UploadActivity.class));
                }
            });
        }
    }

    private void setGridViewOnItemClick() {
        contentGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ObservationEntryObject selectedObject = (ObservationEntryObject) contentGVAdapter.getItem(position);
                Intent intent = new Intent(NewestObservationsActivity.this, ObservationDetailActivity.class);
                intent.putExtra(NID, selectedObject.nid);
                startActivity(intent);
            }
        });
    }

    private void setContentGVOnScroll() {

        // Load data and append to Gridview when scroll to bottom
        contentGV.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /*Make sure that the user touched and scrolled
                * See: http://stackoverflow.com/questions/16073368/onscroll-gets-called-when-i-set-listview-onscrolllistenerthis-but-without-any
                */
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    userScrolled = true;
                else
                    userScrolled=false;
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


                if (userScrolled && totalItemCount < newestObservationCacheManager.getMaxCacheAmount() && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    showLoadingMoreView();
                    /* If the user scrolled to bottom back and forth many times
                     * Don't start a new task to download new data until the old one is done
                     * If there's an old task running, the flag_loading is true;
                    */
                    if (!flag_loading) {
                        HashMap<String, String> sessionInfoMap = new HashMap<>();
                        sessionInfoMap.put(DrupalServicesFieldKeysConst.LOGIN_COOKIE, cookie);
                        sessionInfoMap.put(ACTION, Action.APPEND.toString());
                        ObservationEntryObject lastobject = null;
                        synchronized (gvDataset) {
                            if (gvDataset.size() > 0)
                                lastobject = (ObservationEntryObject) contentGVAdapter.getItem(gvDataset.size() - 1);
                        }
                        if (lastobject != null) {
                            sessionInfoMap.put(DATE, lastobject.date);
                            downloadObservationObjectTask = new DownloadObservationObjectTask((Activity) view.getContext());
                            downloadObservationObjectTask.execute(sessionInfoMap);
                            flag_loading = true;
                        }
                    }
                } else if (totalItemCount >= newestObservationCacheManager.getMaxCacheAmount()) {
                    showNoMoreObservationView();
                }
            }
        });
    }

    private void setToolbarNavigationOnClick() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.root_dl_NewestObservationActivity);
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
    }

    private void setSlidingMenuOnItemClick() {
        ((ListView) findViewById(R.id.sliding_menu)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (((SlidingMenuItem) slidingMenuAdapter.getItem(position)).itemType) {
                    case UPLOAD_ITEM:
                        startActivity(new Intent(NewestObservationsActivity.this, UploadActivity.class));
                        break;
                    case SEARCH_ITEM:
                        startActivity(new Intent(NewestObservationsActivity.this, SearchObservationActivity.class));
                        break;
                    case USER_ACCOUNT_ITEM:
                        startActivity(new Intent(NewestObservationsActivity.this, UserProfileActivity.class));
                        break;
                    case LOGIN_ITEM:
                        startActivity(new Intent(NewestObservationsActivity.this, LoginActivity.class));
                        break;
                }
            }
        });
    }

    private void setSwipeRefreshOnRefresh() {
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
        //Double clicke the tool bar to scroll to top and refresh
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // How much time passed since last click
                long currentTimeInMillSecond = System.currentTimeMillis();
                double timePassed = (currentTimeInMillSecond - toolbarTitleClickedTimeStamp) / 1000;
                if (timePassed < 0.3) {
                    scrollGridViewToTop();
                    showRefreshView();
                    beginDownloadObservation();
                }
                toolbarTitleClickedTimeStamp = currentTimeInMillSecond;
            }
        });
    }

    //Begin AsyncTasks
    private void beginValidateSession() {
        //Start validating if the session is expired or not
        if (!cookie.isEmpty() && PreferenceUtil.getCurrentUserStatus(this)) {
            HashMap<String, String> sessionInfoMap = new HashMap<>();
            sessionInfoMap.put(DrupalServicesFieldKeysConst.LOGIN_COOKIE, cookie);
            validateSessionTask = new ValidateSessionTask(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                validateSessionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
            } else {
                validateSessionTask.execute(sessionInfoMap);
            }
        }
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

    //Helper functions
    private void addGridViewFooter() {
        //Add footer view to GridView to show the loading status
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View footerView = layoutInflater.inflate(R.layout.scrollview_footer_loading, contentGV, false);
        footerView.setTag("footer");
        contentGV.addFooterView(footerView);
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
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showLoadingMoreView() {
        if (gridViewFooterImageView == null)
            gridViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (gridViewFooterTextView == null)
            gridViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if (gridViewFooterImageView != null) {
            if (gridViewFooterImageView.getVisibility() == View.GONE)
                gridViewFooterImageView.setVisibility(View.VISIBLE);
            if (gridViewFooterImageView.getAnimation() == null) {
                RotateAnimation rotateAnimation = AnimationUtil.getRotateAnimation();
                gridViewFooterImageView.startAnimation(rotateAnimation);
            }
        }
        if (gridViewFooterTextView != null) {
            if (gridViewFooterTextView.getVisibility() != View.VISIBLE)
                gridViewFooterTextView.setVisibility(View.VISIBLE);
            gridViewFooterTextView.setText(R.string.loading_newestObservationActivity);
        }
    }

    private void hideLoadingMoreView() {
        if (gridViewFooterImageView == null)
            gridViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (gridViewFooterTextView == null)
            gridViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if (gridViewFooterImageView != null) {
            if (gridViewFooterImageView.getAnimation() != null) {
                gridViewFooterImageView.setAnimation(null);
            }
            if (gridViewFooterImageView.getVisibility() != View.INVISIBLE)
                gridViewFooterImageView.setVisibility(View.INVISIBLE);
        }
        if (gridViewFooterTextView != null) {
            if (gridViewFooterTextView.getVisibility() != View.INVISIBLE)
                gridViewFooterTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void showNoMoreObservationView() {
        if (gridViewFooterImageView == null)
            gridViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (gridViewFooterTextView == null)
            gridViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if (gridViewFooterImageView != null) {
            gridViewFooterImageView.setAnimation(null);
            gridViewFooterImageView.setVisibility(View.GONE);
        }
        if (gridViewFooterTextView != null) {
            gridViewFooterTextView.setText(R.string.no_more_newestObservationActivity);
        }
    }

    private void showLoadingMoreFailedView() {
        if (gridViewFooterImageView == null)
            gridViewFooterImageView = (ImageView) findViewById(R.id.imgLoading_ScrollViewFooterLoading);
        if (gridViewFooterTextView == null)
            gridViewFooterTextView = (TextView) findViewById(R.id.txtLoading_ScrollViewFooterLoading);

        if (gridViewFooterImageView != null) {
            gridViewFooterImageView.setAnimation(null);
            gridViewFooterImageView.setVisibility(View.GONE);
        }
        if (gridViewFooterTextView != null) {
            gridViewFooterTextView.setText(R.string.network_error);
        }
    }

    private void scrollGridViewToTop(){
        if (contentGV==null)
            contentGV=(GridViewWithHeaderAndFooter)findViewById(R.id.content_gridview_NewestObsrvationActivity);
        if (contentGV!=null)
           contentGV.smoothScrollToPosition(0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_newest_observations, menu);
        return true;
    }

    /*CacheObservationObjectTask is not canceled because it could complete in a short time
    * The same as RefreshContentViewDataSetTask*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadObservationObjectTask != null)
            downloadObservationObjectTask.cancel(true);
        if (detectPictureExistTask != null)
            detectPictureExistTask.cancel(true);
        if (validateSessionTask != null)
            validateSessionTask.cancel(true);
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
                        Document photoHtml = Jsoup.parseBodyFragment(observationJsonObject.getString(ObservationModelConst.PHOTO));
                        photoUri = photoHtml.body().getElementsByTag("img").attr("src");
                    } catch (Exception e) {
                    }
                    String title = observationJsonObject.getString(ObservationModelConst.TITLE);
                    String date = observationJsonObject.getString(ObservationModelConst.OBSERVATION_DATE);
                    String nid = observationJsonObject.getString(ObservationModelConst.NID);
                    String author = observationJsonObject.getString(ObservationModelConst.AUTHOR_NAME);

                    ObservationEntryObject object = new ObservationEntryObject();
                    object.title = title;
                    object.photoServerUri = photoUri;
                    object.date = date;
                    object.nid = nid;
                    object.author = author;
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

    private BasicNameValuePair[] getParamsFromDate(String dateTime) {
        if (dateTime != null && !dateTime.isEmpty()) {
            String date = dateTime.split(" ")[0];
            String year = date.split("-")[0];
            String month = date.split("-")[1];
            String day = date.split("-")[2];
            year = String.valueOf(Integer.parseInt(year));
            month = String.valueOf(Integer.parseInt(month));
            day = String.valueOf(Integer.parseInt(day));

            BasicNameValuePair[] returnPairs = new BasicNameValuePair[3];
            returnPairs[0] = new BasicNameValuePair("field_date_observed_value[value][year]", year);
            returnPairs[1] = new BasicNameValuePair("field_date_observed_value[value][month]", month);
            returnPairs[2] = new BasicNameValuePair("field_date_observed_value[value][day]", day);
            return returnPairs;
        }
        return new BasicNameValuePair[0];
    }

    /*****************************
     * AsyncTasks
     *************************************************/

    /* This task validate if the session expired
    *  If yes, show notification in notification bar
    *  and change the SessionExpired in Shared Preferences to true*/
    private class ValidateSessionTask extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>> {
        Activity context;

        public ValidateSessionTask(Activity context) {
            this.context = context;
        }

        @Override
        protected HashMap<String, String> doInBackground(HashMap<String, String>... maps) {
            DrupalAuthSession authSession = new DrupalAuthSession();
            DrupalServicesUser serviceUser = new DrupalServicesUser(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            //If the user login before,his/her info would be in Shared Preferences, otherwise return false
            String cookie = maps[0].get(DrupalServicesFieldKeysConst.LOGIN_COOKIE);
            authSession.setSession(cookie);
            serviceUser.setAuth(authSession);
            try {
                return serviceUser.getSessionInfo();
            } catch (Exception ex) {
                return new HashMap<>();
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, String> sessionInfoMap) {
            if (!sessionInfoMap.isEmpty()) {
                String rolesStr = "";
                if (sessionInfoMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
                    try {
                        JSONObject responseJsonObject = new JSONObject(sessionInfoMap.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
                        JSONObject userJsonObject = responseJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_USER);
                        JSONObject rolesJsonObject = userJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_ROLES);

                        Iterator<String> keys = rolesJsonObject.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            rolesStr += rolesJsonObject.getString(key) + ",";
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //if it's anonymous, session expired
                    if (!rolesStr.isEmpty() && rolesStr.contains(DrupalServicesFieldKeysConst.LOGIN_ROLE_ANONYMOUS)) {
                        PreferenceUtil.saveBoolean(context, SharedPreferencesConst.K_SESSION_EXPIRED, true);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context).
                                        setSmallIcon(R.drawable.refresh_icon).
                                        setContentTitle(getText(R.string.sessionExpiredNotificationTitle)).
                                        setContentText(getText(R.string.sessionExpiredNotificationText));
                        Intent resultIntent = new Intent(context, LoginActivity.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
                        //  stackBuilder.addParentStack(ResultActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(AppConst.SESSION_EXPIRED_NOTIFICATION_ID, mBuilder.build());
                    } else {
                        PreferenceUtil.saveBoolean(context, SharedPreferencesConst.K_SESSION_EXPIRED, false);
                    }
                } else {
                    Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* These 3 classes are called in a consecutive way:
    * DonwloadObservationObjectTask--->CacheObservationObjectTask--->RefreshContentViewDataSetTask
    * First fetch observations in Json and construct an array of Observation Objects
    * Then pass it to Cache Manager to cache them to database
    * Lastly fetch from database to refresh the gridview (text only without picture)
    */
    private class DownloadObservationObjectTask extends AsyncTask<HashMap<String, String>, Void, ObservationEntryObject[]> {
        Activity context;
        String action;

        public DownloadObservationObjectTask(Activity context) {
            this.context = context;
        }

        @Override
        protected ObservationEntryObject[] doInBackground(HashMap<String, String>... params) {
            DrupalAuthSession authSession = new DrupalAuthSession();
            DrupalServicesView newestObservationView = new DrupalServicesView(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            action = params[0].get(ACTION);

            //Get cookie
            String cookie = params[0].get(DrupalServicesFieldKeysConst.LOGIN_COOKIE);
            authSession.setSession(cookie);
            newestObservationView.setAuth(authSession);
            //Get date parameter
            String date = params[0].get(DATE);


            BasicNameValuePair[] pairs = getParamsFromDate(date);

            try {
                HashMap<String, String> responseMap = newestObservationView.retrieve(DrupalServicesView.ViewType.NEWEST_OBSERVATION, pairs);
                return getObservationObjectsFromResponse(responseMap);
            } catch (Exception e) {
                return new ObservationEntryObject[0];
            }
        }

        @Override
        protected void onPostExecute(ObservationEntryObject[] observationEntryObjects) {
            //If download error, only show network unavailable when user wants to refresh
            if (observationEntryObjects.length==0&&action.equals(Action.REFRESH.toString()))
                Toast.makeText(context,R.string.network_error,Toast.LENGTH_SHORT).show();

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
                newestObservationCacheManager.cache(observationEntryObjects);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshGVDatasetTask = new RefreshGVDatasetTask(context, action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                refreshGVDatasetTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                refreshGVDatasetTask.execute();
            }
        }
    }

    private class RefreshGVDatasetTask extends AsyncTask<Void, Void, ArrayList<ObservationEntryObject>> {
        Activity context;
        String action;
        boolean itemAmountChanged;

        public RefreshGVDatasetTask(Activity context, String action) {
            this.context = context;
            this.action = action;
        }

        /*If the user refreshed the view, set the loaded page back to 0
        * Then fetch required amount of data from database
        * Pass the fetched data to onPostExecute()*/
        @Override
        protected ArrayList<ObservationEntryObject> doInBackground(Void... params) {
            if (action.equals(Action.REFRESH.toString())) {
                loadedPage = 0;
            }
            ArrayList<ObservationEntryObject> newList;
            newList = newestObservationCacheManager.getCache((loadedPage + 1) * itemsPerPage);
            return newList;
        }

        /* First check if the new dataset has the same amount of data as the existing adapter dataset
        *  This check is for Append Action
        *  If yes, it means there's no more data in database to load and append
        *  (Though some old data may be updated or replaced, but there's no need to check so accurately in this activity)
        *  Or if the user refreshed the view, we need to refresh the adapter dataset */
        @Override
        protected void onPostExecute(ArrayList<ObservationEntryObject> newList) {
            if (contentGV == null)
                return;

            synchronized (gvDataset) {
                itemAmountChanged = (newList.size() != gvDataset.size());
                if (itemAmountChanged || action.equals(Action.REFRESH.toString())) {
                    gvDataset.clear();
                    for (ObservationEntryObject object : newList) {
                        gvDataset.add(object);
                    }
                    loadedPage++;
                    contentGVAdapter.notifyDataSetChanged();
                }
                if (contentGV.getVisibility() != View.VISIBLE)
                    contentGV.setVisibility(View.VISIBLE);
                if (gvDataset.size() <= 0||(action.equals(Action.APPEND.toString())&&!itemAmountChanged))
                    showNoMoreObservationView();
                else if (action.equals(Action.REFRESH.toString()))
                    scrollGridViewToTop();
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

                /* Lock the gridview dataset
                 * Get all visible objects */
                synchronized (gvDataset) {
                    visibleObjects.clear();
                    if (contentGVAdapter != null&&gvDataset.size()>0) {
                        int firstVisiblePosition = contentGV.getFirstVisiblePosition();
                        int lastVisiblePosition = contentGV.getLastVisiblePosition();
                        if (lastVisiblePosition >= gvDataset.size())
                            lastVisiblePosition = gvDataset.size() - 1;
                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++)
                            visibleObjects.add(gvDataset.get(i));
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
                newestObservationCacheManager.updateRecordImageLocation(objectsLackPicture);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            if (contentGVAdapter != null)
                contentGVAdapter.notifyDataSetChanged();
        }
    }
}