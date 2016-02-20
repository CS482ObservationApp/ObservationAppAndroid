package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    private boolean currentUserExist = false;
    private static final int FIRST_REQUEST_CODE = 100;
    private String TAG ="SPLASHACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        hideStatusBarActionBar();

        Thread initializerThread = new Thread() {
            public void run() {
                //TODO: initialize DB or other stuff

                currentUserExist = getCurrentUserStatus();
                Intent intent;
                if (!currentUserExist) {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, NewestObservationsActivity.class);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG,e.getMessage());
                }
                startActivity(intent);
            }
        };
        initializerThread.start();


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

    private boolean getCurrentUserStatus() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("currentUserExist", false);
    }
}
