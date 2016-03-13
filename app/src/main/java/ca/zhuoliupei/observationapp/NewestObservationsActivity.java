package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import Const.DrupalServicesResponseConst;
import Const.HTTPConst;
import Const.ObservationModelConst;
import Const.SharedPreferencesConst;
import DBContract.ObservationContract;
import DBHelper.NewestObservationDBHelper;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.DownLoadUtil;
import HelperClass.NewestObservationCacheManager;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Model.ObservationObject;
import Model.SlidingMenuItem;

public class NewestObservationsActivity extends AppCompatActivity {

    private enum Action{
        REFRESH,APPEND
    }
    private int loadedPage=0;
    private int itemsPerPage=10;
    private String cacheFolder;
    private final int maxCacheAmount=200;
    private ArrayList<ObservationObject> gvDataset;
    private String cookie;
    private boolean userScrolled=false;
    private boolean flag_loading=false;
    private DetectPictureExistTask detectPictureExistTask;
    private DownloadObsservationObjectTask downloadObsservationObjectTask;
    private CacheObservationObjectTask cacheObservationObjectTask;
    private RefreshGVDatasetTask refreshGVDatasetTask;
    private ValidateSessionTask validateSessionTask;
    private ImageView refreshImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newest_observations);
        initializeVariables();
        initializeUI();
        setWidgetListeners();

        beginValidateSessionTask();
        beginDownloadObservationAsynctask();
        beginDetectPictureAsyncTask();
    }

    private void initializeVariables(){
        cacheFolder= getCacheDir()+"//Newest_Observation";
        gvDataset=new ArrayList<>();
    }
    private void initializeUI(){
        initializeToolBar();
        initializeContentView();
        initializeSlidingMenu();
    }
    private void setWidgetListeners(){
        setFloatingButtonOnClick();
        setGridViewOnItemClick();
        setContentGVOnScroll();
    }

    //Wrapped in initializeUI()
    private void initializeContentView(){
        //Init view
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra("OBSERVATION_OBJECTS");
        if (parcelables!=null&&parcelables.length>0) {
            //Fill gridview content
            for (int i = 0; i < parcelables.length; i++) {
                gvDataset.add((ObservationObject) parcelables[i]);
            }
            NewestObservationAdapter adapter=new NewestObservationAdapter(gvDataset,this);
            GridView gridview=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);
            gridview.setAdapter(adapter);
        }else {
            //Hide the grid view
            GridView gridview=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);
            gridview.setVisibility(View.INVISIBLE);

            //Show refreshing icon
            refreshImgView=new ImageView(this);
            refreshImgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            refreshImgView.setImageResource(R.drawable.refresh_icon);
            //refreshImgView.setId(refreshIconID);
            ((LinearLayout) findViewById(R.id.root_LV_NewestObservationActivity)).addView(refreshImgView);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setDuration(1000);
            refreshImgView.startAnimation(rotateAnimation);
        }
    }
    private void initializeSlidingMenu(){
        //TODO:
        ListView slidingMenuList=(ListView)findViewById(R.id.sliding_menu);
        ArrayList<SlidingMenuItem> items=new ArrayList<>();

        if (PreferenceUtil.getCurrentUserStatus(this)) {
            SlidingMenuItem accountItem = new SlidingMenuItem(SlidingMenuItem.ItemType.USER_ACCOUNT_ITEM);
            accountItem.username = PreferenceUtil.getCurrentUser(this);
            accountItem.location = PreferenceUtil.getCurrentUserLocation1(this);
            accountItem.imgFileLocation=PreferenceUtil.getCurrentUserPictureLocalUri(this);
            items.add(accountItem);
        }else {
            SlidingMenuItem loginItem = new SlidingMenuItem(SlidingMenuItem.ItemType.LOGIN_ITEM);
            items.add(loginItem);
        }
        SlidingMenuItem uploadItem=new SlidingMenuItem(SlidingMenuItem.ItemType.NORMAL_ITEM);
        uploadItem.text="Upload";
        SlidingMenuItem searchItem=new SlidingMenuItem(SlidingMenuItem.ItemType.NORMAL_ITEM);
        searchItem.text="Search";
        SlidingMenuItem userGuideItem=new SlidingMenuItem(SlidingMenuItem.ItemType.NORMAL_ITEM);
        userGuideItem.text="User Guide";

        items.add(uploadItem);
        items.add(searchItem);
        items.add(userGuideItem);

        SlidingMenuAdapter adapter=new SlidingMenuAdapter(items,this);

        slidingMenuList.setAdapter(adapter);
    }
    private void initializeToolBar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_NewestActivity);
        ToolBarStyler.styleToolBar(this,myToolbar,"Newest Observations");
    }

    //Wrapped in setWidgetListeners()
    private void setFloatingButtonOnClick(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    private void setGridViewOnItemClick(){
        GridView gridview=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridView gridview = (GridView) findViewById(R.id.content_gridview_NewestObsrvationActivity);
                NewestObservationAdapter adapter = (NewestObservationAdapter) gridview.getAdapter();
                ObservationObject selectedObject = (ObservationObject) adapter.getItem(position);
                Intent intent = new Intent(NewestObservationsActivity.this, ObservationDetailActivity.class);
                intent.putExtra("SELECT_OBSERVATION_OBJECT", selectedObject);
                startActivity(intent);
            }
        });
    }
    private void setContentGVOnScroll(){
        // Load data and append to Gridview when scroll to bottom
        final GridView contentGV=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);

        contentGV.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userScrolled && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (!flag_loading) {
                        flag_loading = true;
                        HashMap<String, String> sessionInfoMap = new HashMap<>();
                        sessionInfoMap.put(DrupalServicesResponseConst.LOGIN_COOKIE, cookie);
                        sessionInfoMap.put("Action", Action.APPEND.toString());
                        NewestObservationAdapter adapter = (NewestObservationAdapter) view.getAdapter();
                        ObservationObject lastobject = (ObservationObject) adapter.getItem(adapter.getCount() - 1);
                        sessionInfoMap.put("date", lastobject.date);
                        downloadObsservationObjectTask = new DownloadObsservationObjectTask((Activity) view.getContext());
                        downloadObsservationObjectTask.execute(sessionInfoMap);
                    }
                }
            }
        });
    }

    private void beginValidateSessionTask(){
        //Start validating if the session is expired or not
        cookie = PreferenceUtil.getCookie(this);
        if (!cookie.isEmpty()) {
            HashMap<String, String> sessionInfoMap = new HashMap<>();
            sessionInfoMap.put(DrupalServicesResponseConst.LOGIN_COOKIE, cookie);
            validateSessionTask=new ValidateSessionTask(this);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                validateSessionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sessionInfoMap);
            }else {
                validateSessionTask.execute(sessionInfoMap);
            }
        }
    }
    private void beginDownloadObservationAsynctask(){
        HashMap<String, String> sessionInfoMap = new HashMap<>();
        sessionInfoMap.put(DrupalServicesResponseConst.LOGIN_COOKIE, cookie);
        sessionInfoMap.put("Action",Action.REFRESH.toString());
        downloadObsservationObjectTask=new DownloadObsservationObjectTask(this);
        detectPictureExistTask =new DetectPictureExistTask(this);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            downloadObsservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
        }else {
            downloadObsservationObjectTask.execute(sessionInfoMap);
        }
    }
    private void beginDetectPictureAsyncTask(){
        HashMap<String, String> sessionInfoMap = new HashMap<>();
        sessionInfoMap.put(DrupalServicesResponseConst.LOGIN_COOKIE, cookie);
        sessionInfoMap.put("Action", Action.REFRESH.toString());
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            detectPictureExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
        }else {
            detectPictureExistTask.execute(sessionInfoMap);
        }}


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
    * The same as RefreshGVDatasetTask*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadObsservationObjectTask.cancel(true);
        detectPictureExistTask.cancel(true);
        validateSessionTask.cancel(true);
    }


    private ObservationObject[] getObservationObjectsFromResponse(HashMap<String, String> responseMap) {
        // Fetch info from json response, store the image url and other info into each observation objects
        if (responseMap.get(DrupalServicesResponseConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
            try{
                JSONArray responseJsonArray=new JSONArray(responseMap.get(DrupalServicesResponseConst.RESPONSE_BODY));
                ObservationObject[] observationObjects=new ObservationObject[responseJsonArray.length()];
                for (int i=0;i<responseJsonArray.length();i++){
                    JSONObject observationJsonObject=responseJsonArray.getJSONObject(i);

                    String photoUri="";
                    try{
                        Document photoHtml=Jsoup.parseBodyFragment(observationJsonObject.getString(ObservationModelConst.PHOTO));
                        photoUri=photoHtml.body().getElementsByTag("img").attr("src") ;
                    }catch (Exception e){}
                    String title=observationJsonObject.getString(ObservationModelConst.TITLE);
                    String date=observationJsonObject.getString(ObservationModelConst.OBSERVATION_DATE);
                    String nid=observationJsonObject.getString(ObservationModelConst.NID);
                    String author=observationJsonObject.getString(ObservationModelConst.AUTHOR_NAME);

                    ObservationObject object=new ObservationObject();
                    object.title=title;
                    object.photoServerUri =photoUri;
                    object.date=date;
                    object.nid=nid;
                    object.author=author;
                    observationObjects[i]=object;
                }
                return observationObjects;
            }catch (Exception ex){
                return new ObservationObject[0];
            }
        } else {
            return new ObservationObject[0];
        }
    }
    private BasicNameValuePair[] getParamsFromDate(String dateTime){
       if (dateTime!=null&&!dateTime.isEmpty())
       {
           String date=dateTime.split(" ")[0];
           String year=date.split("-")[0];
           String month=date.split("-")[1];
           String day=date.split("-")[2];
           year=String.valueOf(Integer.parseInt(year));
           month=String.valueOf(Integer.parseInt(month));
           day=String.valueOf(Integer.parseInt(day));

           BasicNameValuePair[] returnPairs=new BasicNameValuePair[3];
           returnPairs[0]=new BasicNameValuePair("field_date_observed_value[value][year]",year);
           returnPairs[1]=new BasicNameValuePair("field_date_observed_value[value][month]",month);
           returnPairs[2]=new BasicNameValuePair("field_date_observed_value[value][day]",day);
           return returnPairs;
       }
        return new BasicNameValuePair[0];
    }

/*****************************        AsyncTasks         *************************************************/

    /* This task validate if the session expired
    *  If yes, show notification in notification bar
    *  and change the SessionExpired in Shared Preferences to true*/
    private class ValidateSessionTask extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>> {
        Activity context;
        public ValidateSessionTask(Activity context){
            this.context=context;
        }
        @Override
        protected HashMap<String, String> doInBackground(HashMap<String, String>... maps) {
            DrupalAuthSession authSession = new DrupalAuthSession();
            DrupalServicesUser serviceUser = new DrupalServicesUser(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            //If the user login before,his/her info would be in Shared Preferences, otherwise return false
            String cookie = maps[0].get(DrupalServicesResponseConst.LOGIN_COOKIE);
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
                if (sessionInfoMap.get(DrupalServicesResponseConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
                    try {
                        JSONObject responseJsonObject = new JSONObject(sessionInfoMap.get(DrupalServicesResponseConst.RESPONSE_BODY));
                        JSONObject userJsonObject = responseJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_USER);
                        JSONObject rolesJsonObject = userJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_ROLES);

                        Iterator<String> keys = rolesJsonObject.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            rolesStr += rolesJsonObject.getString(key) + ",";
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //if it's anonymous, session expired
                    if (!rolesStr.isEmpty() && rolesStr.contains(DrupalServicesResponseConst.LOGIN_ROLE_ANONYMOUS)) {
                        PreferenceUtil.saveBoolean(context, SharedPreferencesConst.K_SESSION_EXPIRED, true);
                        //TODO:Show message in notification bar
                        NotificationCompat.Builder mBuilder=
                                        new NotificationCompat.Builder(context) .
                                        setSmallIcon(R.drawable.refresh_icon).
                                        setContentTitle(getText(R.string.sessionExpiredNotificationTitle)).
                                        setContentText(getText(R.string.sessionExpiredNotificationText));
                        Intent resultIntent=new Intent(context,LoginActivity.class);
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
                        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(AppConst.SESSION_EXPIRED_NOTIFICATION_ID,mBuilder.build());
                    } else {
                       PreferenceUtil.saveBoolean(context, SharedPreferencesConst.K_SESSION_EXPIRED, false);
                    }
                } else {
                    Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    /* These 3 classes are called in a consecutive way:
    * DonwloadObservationObjectTask--->CacheObservationObjectTask--->RefreshGVDatasetTask
    * First fetch observations in Json and construct an array of Observation Objects
    * Then pass it to Cache Manager to cache them to database
    * Lastly fetch from database to refresh the gridview (text only without picture)
    */
    private class DownloadObsservationObjectTask extends AsyncTask<HashMap<String, String>, Void,  ObservationObject[]> {
        Activity context;
        String action;
        public DownloadObsservationObjectTask(Activity context){
            this.context=context;
        }
        @Override
        protected  ObservationObject[] doInBackground(HashMap<String, String>... params) {
            DrupalAuthSession authSession = new DrupalAuthSession();
            DrupalServicesView newestObservationView = new DrupalServicesView(getText(R.string.drupal_site_url).toString(), getText(R.string.drupal_server_endpoint).toString());

            action=params[0].get("Action");

            //Get cookie
            String cookie = params[0].get(DrupalServicesResponseConst.LOGIN_COOKIE);
            authSession.setSession(cookie);
            newestObservationView.setAuth(authSession);
            //Get date parameter
            String date=params[0].get("date");


            BasicNameValuePair[] pairs=getParamsFromDate(date);

            try {
                HashMap<String, String> responseMap = newestObservationView.retrive(DrupalServicesView.View.NEWEST_OBSERVATION, pairs);
                return getObservationObjectsFromResponse(responseMap);
            } catch (Exception e) {
                return new ObservationObject[0];
            }
        }

        @Override
        protected void onPostExecute(ObservationObject[] observationObjects) {
            cacheObservationObjectTask=new CacheObservationObjectTask(context, action);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                cacheObservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,observationObjects);
            }else {
                cacheObservationObjectTask.execute(observationObjects);
            }
        }
    }
    private class CacheObservationObjectTask extends AsyncTask<ObservationObject[],Void,Void>{
        Activity context;
        String action;
        public  CacheObservationObjectTask(Activity context,String action){
            this.context=context;
            this.action=action;
        }
        @Override
        protected Void doInBackground(ObservationObject[]... params) {
            //Cache to database
            ObservationObject[] observationObjects=params[0];
            if (observationObjects.length>0){
                NewestObservationCacheManager newestObservationCacheManager =new NewestObservationCacheManager(cacheFolder,context,maxCacheAmount);
                newestObservationCacheManager.cache(observationObjects);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshGVDatasetTask=new RefreshGVDatasetTask(context,action);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                refreshGVDatasetTask .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                refreshGVDatasetTask.execute();
            }
        }
    }
    private class RefreshGVDatasetTask extends AsyncTask<Void,Void,ArrayList<ObservationObject>>{
        Activity context;
        String action;
        boolean itemAmountChanged;
        public  RefreshGVDatasetTask(Activity context,String action){
            this.context=context;
            this.action=action;
        }
        @Override
        protected ArrayList<ObservationObject> doInBackground(Void... params) {
            NewestObservationCacheManager newestObservationCacheManager =new NewestObservationCacheManager(cacheFolder,context,maxCacheAmount);
            if (action.equals(Action.REFRESH.toString())) {
                loadedPage = 0;
            }
            ArrayList<ObservationObject> newList= newestObservationCacheManager.getCache((++loadedPage) * itemsPerPage);
            itemAmountChanged=(newList.size()!=gvDataset.size());
            gvDataset.clear();
            for (ObservationObject object:newList) {
                gvDataset.add(object);
            }
            return gvDataset;
        }

        @Override
        protected void onPostExecute(ArrayList<ObservationObject> gvDataset) {
            GridView contentGV = (GridView) context.findViewById(R.id.content_gridview_NewestObsrvationActivity);
            if (contentGV==null)
                return;

            NewestObservationAdapter adapter = (NewestObservationAdapter) contentGV.getAdapter();
            if (adapter == null) {
                adapter = new NewestObservationAdapter(gvDataset, context);
                ((GridView) context.findViewById(R.id.content_gridview_NewestObsrvationActivity)).setAdapter(adapter);
                context.findViewById(R.id.content_gridview_NewestObsrvationActivity).setVisibility(View.VISIBLE);
            }
            if (itemAmountChanged)
                adapter.notifyDataSetChanged();
            flag_loading = false;
            if (refreshImgView!=null){
                if (refreshImgView.getAnimation()!=null)
                    refreshImgView.clearAnimation();
                refreshImgView.setVisibility(View.GONE);
            }
        }
    }


    /* This task runs in the back ground until the activity destroyed
    *  It scans through the visible items in GridView to see if the local picture file exits
    *  If not, download the file, and update the location in the database,refresh Gridview adapter*/
    private class DetectPictureExistTask extends AsyncTask<HashMap<String ,String>,Void,Void>{
        Activity context;
        ArrayList<ObservationObject> visibleObjects;
        ArrayList<ObservationObject> objectsLackPicture;
        NewestObservationDBHelper dbHelper;
        SQLiteDatabase writableDatabase;
        GridView contentGV;
        NewestObservationAdapter adapter;
        public DetectPictureExistTask(Activity context){
            this.context=context;
            this.visibleObjects=new ArrayList<>();
            this.objectsLackPicture=new ArrayList<>();
            this.dbHelper=new NewestObservationDBHelper(context);
            this.writableDatabase=dbHelper.getWritableDatabase();
            this.contentGV=(GridView)context.findViewById(R.id.content_gridview_NewestObsrvationActivity);
            if (contentGV!=null){
                adapter=(NewestObservationAdapter)contentGV.getAdapter();
            }
        }
        @Override
        protected Void doInBackground(HashMap<String ,String>... params) {
            while (!this.isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e("DETECT_LACK_PICTURE","Detect Picture Task Sleep interrupted");
                }
                visibleObjects.clear();
                GridView contentGV=(GridView)context.findViewById(R.id.content_gridview_NewestObsrvationActivity);
                adapter=(NewestObservationAdapter)contentGV.getAdapter();
                int firstVisiblePosition =contentGV.getFirstVisiblePosition();
                int lastVisiblePosition =contentGV.getLastVisiblePosition();
                if (adapter!=null){
                    for (int i=firstVisiblePosition;i<=lastVisiblePosition;i++){
                        visibleObjects.add((ObservationObject)adapter.getItem(i));
                    }
                }
                objectsLackPicture.clear();
                for (ObservationObject object:visibleObjects) {
                    if (object.photoLocalUri==null||object.photoLocalUri.isEmpty()||!new File(object.photoLocalUri).exists()){
                        String serverUri=object.photoServerUri;
                        if (serverUri==null||serverUri.isEmpty())
                            continue;
                        String nid=object.nid;
                        String filename=new File(context.getFilesDir(), System.currentTimeMillis()+nid).getPath();

                        //DOWNLOAD IMAGE TO LOCAL
                        boolean downloadSucceed= DownLoadUtil.downloadImage(serverUri,filename);

                        if (downloadSucceed) {
                            //UPDATE DATABASE
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, filename);
                            String whereCause = ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID + " = " + nid;
                            object.photoLocalUri=filename;
                            writableDatabase.update(ObservationContract.NewestObservationEntry.TABLE_NAME, contentValues, whereCause, null);
                            publishProgress();
                        }
                    }
                }
            }
            return  null;
        }

        @Override
        protected void onProgressUpdate(Void...params) {
            if (adapter!=null)
                adapter.notifyDataSetChanged();
        }
    }
}