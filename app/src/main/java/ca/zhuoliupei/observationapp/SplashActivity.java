package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import Const.AppConst;
import Const.DrupalServicesResponseConst;
import DBContract.ObservationContract;
import DBContract.ObservationContract.NewestObservationEntry;
import DBHelper.NewestObservationDBHelper;
import Model.ObservationObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {


    NewestObservationDBHelper mNewestDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarActionBar();

        setContentView(R.layout.activity_splash);

        mNewestDBHelper=new NewestObservationDBHelper(this);

        new InitAppTask().execute();
    }

    private class InitAppTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            boolean currentUserExist = getCurrentUserStatus();
            Intent intent;
            if (!currentUserExist) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, NewestObservationsActivity.class);
                Cursor cursor=getNewestObservationFromDB();
                if (cursor.getCount()!=0){
                    ObservationObject[] observationObjects= getObservationObjectsFromCursor(cursor);
                    intent.putExtra("OBSERVATION_OBJECTS",observationObjects);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                String TAG = "SPLASH_ACTIVITY";
                Log.e(TAG,e.getMessage());
            }
            startActivity(intent);
            return null;
        }

        private Cursor getNewestObservationFromDB(){
            //TODO:Load data from database
            SQLiteDatabase newestObserDB= mNewestDBHelper.getReadableDatabase();
            String[] projection = {
                    NewestObservationEntry.COLUMN_NAME_NODE_ID,
                    NewestObservationEntry.COLUMN_NAME_TITLE,
                    NewestObservationEntry.COLUMN_NAME_ADDRESS,
                    NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI,
                    NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI,
                    NewestObservationEntry.COLUMN_NAME_DATE
            };
            String sortOrder = NewestObservationEntry.COLUMN_NAME_DATE + " DESC";
            return newestObserDB.query(
                    ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder,                                 // The sort order
                    "10"                                      // Limit 10 rows
            );
        }

        private ObservationObject[] getObservationObjectsFromCursor(Cursor cursor){
            ObservationObject[] observationObjects=new ObservationObject[cursor.getCount()];
            cursor.moveToFirst();
            int i=0;
            String EMPTY_ATTRIBUTE="";
            while (!cursor.isAfterLast()) {
                String title = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_TITLE));
                String nid = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID));
                String photoServerUri = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI));
                String photoLocalUri = cursor.getString(cursor.getColumnIndex(NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
                String address = cursor.getString(cursor.getColumnIndex(NewestObservationEntry.COLUMN_NAME_ADDRESS));
                String date = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE));
                observationObjects[i]=new ObservationObject(nid,title,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,photoServerUri,photoLocalUri,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,address,date);
                i++;
                cursor.moveToNext();
            }
            return observationObjects;
        }
    }

    private boolean getCurrentUserStatus() {
        //If the user login before,his/her info would be in Shared Preferences, otherwise return false
        String currentUser=PreferenceManager.getDefaultSharedPreferences(this).getString(DrupalServicesResponseConst.USER,null);
        boolean sessionExpired=PreferenceManager.getDefaultSharedPreferences(this).getBoolean(AppConst.SESSION_EXPIRED, true);
        return currentUser != null&&!sessionExpired;

    }
    private void hideStatusBarActionBar() {
        // If the Android version is lower than Jellybean, use this call to hide the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            // Hide the status bar in Android higher than Jellybean.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        // Hide the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}

//Detect if the session is expired
//authSession.setSession(cookie);
//        serviceUser.setAuth(authSession);
//        HashMap<String,String > sessionInfoMap=serviceUser.getSessionInfo();
//        if (sessionInfoMap.get(Const.STATUSCODE).equals(Const.HTTP_OK_200)){
//        if (sessionInfoMap.get(Const.ROLES).contains(Const.ROLE_ANONYMOUS)){
//        return false;
//        }else{
//        //Put session info into shared preferences
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(Const.ROLES, sessionInfoMap.get(Const.ROLES));
//        editor.putString(Const.SESSIONID, sessionInfoMap.get(Const.SESSIONID));
//        editor.putString(Const.USERID, sessionInfoMap.get(Const.USERID));
//        editor.putString(Const.SESSIONNAME, sessionInfoMap.get(Const.SESSIONNAME));
//        //Get login user name using the uid
//        HashMap<String,String> userInfoMap= serviceUser.getUser(Integer.parseInt(sessionInfoMap.get(Const.USERID)));
//        return true;
//        }
//        }else{
//        return false;
//        }