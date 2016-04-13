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

import HelperClass.NewestObservationCacheManager;
import HelperClass.PreferenceUtil;
import Model.ObservationEntryObject;

/**
 * This activity provides function:
 * 1.Detect if there's a user already login
 * 2.If a current user exists, fetch observations from local database and pass it to Newest Observation Activity
 * 3.Otherwise jump to Login Activity
 */
public class SplashActivity extends AppCompatActivity {

    InitAppTask initAppTask;
    NewestObservationCacheManager cacheManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarActionBar();
        setContentView(R.layout.activity_splash);
        initializeVariables();
        initApp();
    }

    private void initializeVariables(){
        cacheManager=NewestObservationCacheManager.getInstance(this);
    }
    private void initApp(){
        initAppTask=new InitAppTask(this);
        initAppTask.execute();
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

    }


}

