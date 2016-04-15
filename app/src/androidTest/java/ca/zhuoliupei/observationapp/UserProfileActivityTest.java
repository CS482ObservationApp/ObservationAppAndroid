package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class UserProfileActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {



    public UserProfileActivityTest() {

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

    @SmallTest
    public void testUser() {

        Activity activity = loginState();

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(UserProfileActivity.class.getName(), null, false);

        activity.startActivity(new Intent(activity, UserProfileActivity.class));

        Activity profileAct = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);


        TextView username = (TextView) profileAct.findViewById(R.id.txtUserName_UserProfileActivity);

        //proper user profile is displayed
        assertEquals(username.getText(), USERNAME);




    }


}