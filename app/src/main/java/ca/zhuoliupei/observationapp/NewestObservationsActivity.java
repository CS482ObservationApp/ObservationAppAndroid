package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.ProcessingInstruction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Adapter.NewestObservationAdapter;
import Const.AppConst;
import Const.DrupalServicesResponseConst;
import Const.HTTPConst;
import Const.ObservationModelConst;
import DBContract.ObservationContract;
import DBHelper.NewestObservationDBHelper;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesNode;
import DrupalForAndroidSDK.DrupalServicesUser;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.NewestObservationCacheManager;
import Model.ObservationObject;

public class NewestObservationsActivity extends AppCompatActivity {

    private int refreshIconID=1;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViewAndVariables();
        setFloatingButtonOnClick();
        setContentGVOnClick();
        validateSession();
        //Init view
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra("OBSERVATION_OBJECTS");
        InitView(parcelables);
        //Begin to download and refresh
        HashMap<String, String> sessionInfoMap = new HashMap<>();
        sessionInfoMap.put(DrupalServicesResponseConst.COOKIE, cookie);
        sessionInfoMap.put("Action",Action.REFRESH.toString());
        downloadObsservationObjectTask=new DownloadObsservationObjectTask(this);
        detectPictureExistTask =new DetectPictureExistTask(this);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            downloadObsservationObjectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionInfoMap);
            detectPictureExistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sessionInfoMap);
        }else {
            downloadObsservationObjectTask.execute(sessionInfoMap);
            detectPictureExistTask.execute(sessionInfoMap);
        }
    }

    private void initializeViewAndVariables(){
        setContentView(R.layout.activity_newest_observations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        cacheFolder= getCacheDir()+"//Newest_Observation";
        gvDataset=new ArrayList<>();
    }
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
    private void setContentGVOnClick(){
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
                        sessionInfoMap.put(DrupalServicesResponseConst.COOKIE, cookie);
                        sessionInfoMap.put("Action", Action.APPEND.toString());
                        NewestObservationAdapter adapter = (NewestObservationAdapter) view.getAdapter();
                        ObservationObject lastobject = (ObservationObject) adapter.getItem(adapter.getCount() - 1);
                        sessionInfoMap.put("date", lastobject.date);
                        new DownloadObsservationObjectTask((Activity) view.getContext()).execute(sessionInfoMap);
                    }
                }
            }
        });
    }
    private void validateSession(){
        //Start validating if the session is expired or not
        cookie = PreferenceManager.getDefaultSharedPreferences(this).getString(DrupalServicesResponseConst.COOKIE, "");
        if (!cookie.isEmpty()) {
            HashMap<String, String> sessionInfoMap = new HashMap<>();
            sessionInfoMap.put(DrupalServicesResponseConst.COOKIE, cookie);
            new ValidateSessionTask(this).execute(sessionInfoMap);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadObsservationObjectTask.cancel(true);
        detectPictureExistTask.cancel(true);
    }

    private void InitView(Parcelable[] parcelables){
        if (parcelables!=null&&parcelables.length>0) {
            //Fill gridview content
            for (int i = 0; i < parcelables.length; i++) {
                gvDataset.add((ObservationObject) parcelables[i]);
            }
            NewestObservationAdapter adapter=new NewestObservationAdapter(gvDataset,this);
            GridView gridview=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);
            gridview.setAdapter(adapter);
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
        }else {
            //Hide the grid view
            GridView gridview=(GridView)findViewById(R.id.content_gridview_NewestObsrvationActivity);
            gridview.setVisibility(View.INVISIBLE);

            //Show refreshing icon
            ImageView refreshImgView=new ImageView(this);
            refreshImgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            refreshImgView.setImageResource(R.drawable.refresh_icon);
            refreshImgView.setId(refreshIconID);
            ((LinearLayout)findViewById(R.id.root_LV_NewestObservationActivity)).addView(refreshImgView);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setDuration(1000);
            refreshImgView.startAnimation(rotateAnimation);
        }
    }


    private ObservationObject[] FetchInfoFromResponse(HashMap<String, String> responseMap) {
        // Fetch info from json response, store the image url and other info into each observation objects
        if (responseMap.get(DrupalServicesResponseConst.STATUSCODE).equals(HTTPConst.HTTP_OK_200)) {
            try{
                JSONArray responseJsonArray=new JSONArray(responseMap.get(DrupalServicesResponseConst.RESPONSEBODY));
                ObservationObject[] observationObjects=new ObservationObject[responseJsonArray.length()];
                for (int i=0;i<responseJsonArray.length();i++){
                    JSONObject observationJsonObject=responseJsonArray.getJSONObject(i);
                    JSONObject addressJsonObject=observationJsonObject.getJSONObject(ObservationModelConst.ADDRESS);

                    String title=observationJsonObject.getString(ObservationModelConst.TITLE);
                    String address=addressJsonObject.getString(ObservationModelConst.COUNTRY)+","+addressJsonObject.getString(ObservationModelConst.ADMINISTRATIVE_AREA);
                    Document photoHtml=Jsoup.parseBodyFragment(observationJsonObject.getString(ObservationModelConst.PHOTO));
                    String photoUri=photoHtml.body().getElementsByTag("img").attr("src") ;
                    String date=observationJsonObject.getString(ObservationModelConst.OBSERVATION_DATE);
                    String nid=observationJsonObject.getString(ObservationModelConst.NID);

                    ObservationObject object=new ObservationObject();
                    object.title=title;
                    object.address=address;
                    object.photoServerUri =photoUri;
                    object.date=date;
                    object.nid=nid;
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
           returnPairs[0]=new BasicNameValuePair("field_observation_date_value[value][year]",year);
           returnPairs[1]=new BasicNameValuePair("field_observation_date_value[value][month]",month);
           returnPairs[2]=new BasicNameValuePair("field_observation_date_value[value][day]",day);
           return returnPairs;
       }
        return new BasicNameValuePair[0];
    }


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
            String cookie = maps[0].get(DrupalServicesResponseConst.COOKIE);
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
                if (sessionInfoMap.get(DrupalServicesResponseConst.STATUSCODE).equals(HTTPConst.HTTP_OK_200)) {
                    try {
                        JSONObject responseJsonObject = new JSONObject(sessionInfoMap.get(DrupalServicesResponseConst.RESPONSEBODY));
                        JSONObject userJsonObject = responseJsonObject.getJSONObject(DrupalServicesResponseConst.USER);
                        JSONObject rolesJsonObject = userJsonObject.getJSONObject(DrupalServicesResponseConst.ROLES);

                        Iterator<String> keys = rolesJsonObject.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            rolesStr += rolesJsonObject.getString(key) + ",";
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //if it's anonymous, session expired
                    if (!rolesStr.isEmpty() && rolesStr.contains(DrupalServicesResponseConst.ROLE_ANONYMOUS)) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(AppConst.SESSION_EXPIRED, true);
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
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(AppConst.SESSION_EXPIRED, false);
                    }
                } else {
                    Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, getText(R.string.network_error).toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

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
            String cookie = params[0].get(DrupalServicesResponseConst.COOKIE);
            authSession.setSession(cookie);
            newestObservationView.setAuth(authSession);
            //Get date parameter
            String date=params[0].get("date");


            BasicNameValuePair[] pairs=getParamsFromDate(date);

            try {
                HashMap<String, String> responseMap = newestObservationView.retrive(DrupalServicesView.View.NEWEST_OBSERVATION, pairs);
                return FetchInfoFromResponse(responseMap);
            } catch (Exception e) {
                return new ObservationObject[0];
            }
        }

        @Override
        protected void onPostExecute(ObservationObject[] observationObjects) {
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                new CacheObservationObjectTask(context, action).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,observationObjects);
            }else {
                new CacheObservationObjectTask(context, action).execute(observationObjects);
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
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                new RefreshGVDatasetTask(context,action).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                new RefreshGVDatasetTask(context,action).execute();
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
            NewestObservationAdapter adapter = (NewestObservationAdapter) contentGV.getAdapter();
            if (adapter == null) {
                adapter = new NewestObservationAdapter(gvDataset, context);
                ((GridView) context.findViewById(R.id.content_gridview_NewestObsrvationActivity)).setAdapter(adapter);
                context.findViewById(R.id.content_gridview_NewestObsrvationActivity).setVisibility(View.VISIBLE);
            }
            if (itemAmountChanged)
                adapter.notifyDataSetChanged();
            flag_loading = false;
            LinearLayout rootLL=(LinearLayout)findViewById(R.id.root_LV_NewestObservationActivity);
            ImageView refreshIV=(ImageView)findViewById(refreshIconID);
            if (refreshIV!=null){
                if (refreshIV.getAnimation()!=null)
                    refreshIV.getAnimation().cancel();

                refreshIV.setVisibility(View.INVISIBLE);
            }
        }
    }


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
                    Thread.sleep(500);
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
                        String nid=object.nid;
                        String filename=new File(context.getFilesDir(), System.currentTimeMillis()+nid).getPath();
                        //DOWNLOAD IMAGE TO LOCAL
                        boolean downloadSucceed=false;
                        try {
                            URL url = new URL(serverUri);
                            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            FileOutputStream out = new FileOutputStream(filename);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            downloadSucceed=true;
                        }catch (Exception e){
                            Log.e("IMAGE_DOWNLOAD","Cannot download image");
                        }
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

    private class ScanChangeTask extends AsyncTask<Void,Void,Void>{
        Activity context;
        String action;
        public ScanChangeTask(Activity context,String action){
            this.context=context;
            this.action=action;
        }
        @Override
        protected Void doInBackground(Void... params) {
            NewestObservationDBHelper helper=new NewestObservationDBHelper(context);
            SQLiteDatabase readableDatabase=helper.getReadableDatabase();
            SQLiteDatabase writableDatabase=helper.getWritableDatabase();

            String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";

            //Get exist item count
            Cursor cursor= readableDatabase.query(
                    ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                    null,                                     // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
            //If a node is deleted in sever,delete it in local DB too
            cursor.moveToFirst();
            DrupalServicesNode drupalServicesNode=new DrupalServicesNode(context.getText(R.string.drupal_site_url).toString(),context.getText(R.string.drupal_server_endpoint).toString());
            drupalServicesNode.setAuth(new DrupalAuthSession());
            while (!cursor.isAfterLast()){
                String statusCode;
                int nid=Integer.parseInt(cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID)));
                try {
                    //retrieve the node info using the cursor's node id
                    statusCode=drupalServicesNode.retrieve(nid).get(DrupalServicesResponseConst.STATUSCODE);
                }catch (Exception e){
                    cursor.moveToNext();
                    continue;
                }
                if (!statusCode.equals("200")){
                    String whereCause= ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID+"="+nid;
                    writableDatabase.delete(ObservationContract.NewestObservationEntry.TABLE_NAME,whereCause,null);
                }
                cursor.moveToNext();
            }
            return null;
        }
    }

    private class DownloadImageForObjectTask extends AsyncTask<ObservationObject,Void,Void>{
        Activity context;
        NewestObservationDBHelper dbHelper;
        SQLiteDatabase writableDatabase;
        public DownloadImageForObjectTask(Activity context){
            this.context=context;
            dbHelper=new NewestObservationDBHelper(context);
            writableDatabase= dbHelper.getWritableDatabase();
        }
        @Override
        protected Void doInBackground(ObservationObject... params) {
            ObservationObject observationObject=params[0];
            String serverUri=observationObject.photoServerUri;
            String nid=observationObject.nid;
            String filename=new File(cacheFolder, System.currentTimeMillis()+nid).getPath();
            //TODO:DOWNLOAD IMAGE TO LOCAL
            try {
                URL url = new URL(serverUri);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                FileOutputStream out = new FileOutputStream(filename);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }catch (Exception e){Log.e("IMAGE_DOWNLOAD","Cannot download image");}
            //TODO:UPDATE DATABASE
            ContentValues contentValues=new ContentValues();
            contentValues.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, filename);
            String whereCause= ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID+" = "+nid;
            writableDatabase.update(ObservationContract.NewestObservationEntry.TABLE_NAME,contentValues,whereCause,null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ( (NewestObservationAdapter)((GridView)context.findViewById(R.id.content_gridview_NewestObsrvationActivity)).getAdapter()).notifyDataSetChanged();
        }
    }

}