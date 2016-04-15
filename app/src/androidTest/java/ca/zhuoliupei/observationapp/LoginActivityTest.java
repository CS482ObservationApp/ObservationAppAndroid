package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;


public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {



    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    final String USERNAME = "NormalUser";
    final String PASSWORD = "kentlee1993";

    public Activity loginState()
    {
        getActivity().finish();
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        Instrumentation.ActivityMonitor loginMonitor = getInstrumentation().addMonitor(LoginActivity.class.getName(), null, false);
        this.setActivity(getInstrumentation().waitForMonitorWithTimeout(loginMonitor, 10000));
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(NewestObservationsActivity.class.getName(), null, false);
        final Button loginButton = (Button) getActivity().findViewById(R.id.btnLogin_LoginActivity);

        assertTrue(loginButton.isClickable()); //check if loginButton is clickable
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {

                ((TextView) getActivity().findViewById(R.id.txtUserName_LoginActivity)).setText(USERNAME);
                ((TextView) getActivity().findViewById(R.id.txtPassword_LoginActivity)).setText(PASSWORD);

                loginButton.performClick();

            }
        });
        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);

        return nextActivity;
    }

    //Try to login with valid username and password
    @Test
    public void testLoginValidUserName()
    {
        Activity nextActivity = loginState();
        //successfully went to the next activity
        assertNotNull(nextActivity);

        nextActivity.finish();
    }


    //Try to login with invalid user
    @Test
    public void testLoginInvalidUser()
    {

        getActivity().finish();
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        Instrumentation.ActivityMonitor loginMonitor = getInstrumentation().addMonitor(LoginActivity.class.getName(), null, false);
        this.setActivity(getInstrumentation().waitForMonitorWithTimeout(loginMonitor, 10000));

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(NewestObservationsActivity.class.getName(), null, false);
        final Button loginButton = (Button) getActivity().findViewById(R.id.btnLogin_LoginActivity);

        assertTrue(loginButton.isClickable()); //check if loginButton is clickable

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // click button and open next activity.
                ((TextView) getActivity().findViewById(R.id.txtUserName_LoginActivity)).setText("Norma222lUser");
                loginButton.performClick();

            }
        });


        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);

        //did not make it to the next activity
        assertNull(nextActivity);

    }



}

