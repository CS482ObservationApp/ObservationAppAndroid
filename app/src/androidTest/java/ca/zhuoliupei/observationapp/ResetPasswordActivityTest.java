package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;


public class ResetPasswordActivityTest extends ActivityInstrumentationTestCase2<ResetPasswordActivity> {

    final String USERNAME = "NormalUser";

    public ResetPasswordActivityTest() {
        super(ResetPasswordActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    public void testResetValidUsername() {

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(NewestObservationsActivity.class.getName(), null, false);

        final Button submit = (Button) getActivity().findViewById(R.id.imgBtnSubmit_ResetPasswordActivity);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {

                ((TextView) getActivity().findViewById(R.id.txtUsernameEmail_ResetPasswordActivity)).setText(USERNAME);

                submit.performClick();
                TextView message = (TextView) getActivity().findViewById(R.id.txtMessage_RestPasswordActivity);

            }
        });


        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        TextView message = (TextView) getActivity().findViewById(R.id.txtMessage_RestPasswordActivity);


        message.getText().toString().contains(USERNAME);

        assertTrue(!message.getText().toString().toLowerCase().contains(USERNAME.toLowerCase()));


    }


}
