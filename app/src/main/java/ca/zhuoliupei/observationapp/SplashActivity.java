package ca.zhuoliupei.observationapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import DBHelper.NewestObservationDBHelper;
import HelperClass.NewestObservationCacheManager;
import HelperClass.PreferenceUtil;
import Model.ObservationEntryObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    InitAppTask initAppTask;
    NewestObservationDBHelper mNewestDBHelper;
    NewestObservationCacheManager cacheManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarActionBar();
        setContentView(R.layout.activity_splash);
        initializeVariables();
    }

    private void initializeVariables(){
        mNewestDBHelper=new NewestObservationDBHelper(this);
        cacheManager=NewestObservationCacheManager.getInstance(this);
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
                ArrayList<ObservationEntryObject> observationEntryObjectList =getNewestObservationFromDB();
                if (observationEntryObjectList.size()!=0){
                    ObservationEntryObject[] observationEntryObjects = getObservationObjectArrayFromArrayList(observationEntryObjectList);
                    intent.putExtra("OBSERVATION_OBJECTS", observationEntryObjects);
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
    private ArrayList<ObservationEntryObject> getNewestObservationFromDB(){
        return cacheManager.getCache(10);
    }

    private ObservationEntryObject[] getObservationObjectArrayFromArrayList(ArrayList<ObservationEntryObject> objects){
        ObservationEntryObject[] observationEntryObjects =new ObservationEntryObject[objects.size()];
        String EMPTY_ATTRIBUTE="";
        int i=0;
        for (ObservationEntryObject object:objects) {
            String title = object.title;
            String nid = object.nid;
            String photoServerUri = object.photoServerUri;
            String photoLocalUri = object.photoLocalUri;
            String author = object.author;
            String date = object.date;
            observationEntryObjects[i]=new ObservationEntryObject(nid,title,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,photoServerUri,photoLocalUri,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,EMPTY_ATTRIBUTE,date,author);
            i++;
        }
        return observationEntryObjects;
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
        if (initAppTask!=null)
            initAppTask.cancel(true);
    }
}

