package ca.zhuoliupei.observationapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import Const.AppConst;
import Const.DrupalServicesResponseConst;
import DBContract.ObservationContract;
import DBContract.ObservationContract.NewestObservationEntry;
import DBHelper.NewestObservationDBHelper;
import HelperClass.NewestObservationCacheManager;
import HelperClass.PreferenceUtil;
import Model.ObservationObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    InitAppTask initAppTask;
    NewestObservationDBHelper mNewestDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarActionBar();

        setContentView(R.layout.activity_splash);

        mNewestDBHelper=new NewestObservationDBHelper(this);
        initAppTask=new InitAppTask(this);
        initAppTask.execute();
    }

    private class InitAppTask extends AsyncTask<Void,Void,Void>{
        Context context;
        public InitAppTask(Context context){
            this.context=context;
        }
        @Override
        protected Void doInBackground(Void... params) {
            boolean currentUserExist = PreferenceUtil.getCurrentUserStatus(context);
            Intent intent;
            if (!currentUserExist) {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, NewestObservationsActivity.class);
                ArrayList<ObservationObject> observationObjectList=getNewestObservationFromDB();
                if (observationObjectList.size()!=0){
                    ObservationObject[] observationObjects= getObservationObjectArrayFromArrayList(observationObjectList);
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


    }
    private ArrayList<ObservationObject> getNewestObservationFromDB(){
        NewestObservationCacheManager cacheManager=new NewestObservationCacheManager(this);
        return cacheManager.getCache(10);
    }

    private ObservationObject[] getObservationObjectArrayFromArrayList(ArrayList<ObservationObject> objects){
        ObservationObject[] observationObjects=new ObservationObject[objects.size()];
        String EMPTY_ATTRIBUTE="";
        int i=0;
        for (ObservationObject object:objects) {
            String title = object.title;
            String nid = object.nid;
            String photoServerUri = object.photoServerUri;
            String photoLocalUri = object.photoLocalUri;
            String author = object.author;
            String date = object.date;
            observationObjects[i]=new ObservationObject(nid,title,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,photoServerUri,photoLocalUri,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,date,author);
            i++;
        }
        return observationObjects;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        initAppTask.cancel(true);
    }
}

