package ca.zhuoliupei.observationapp;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

/**
 *
 */
public class RegisterActivityTest extends ActivityInstrumentationTestCase2<RegisterActivity> {

    public RegisterActivityTest() {
        super(RegisterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    final String USERNAME = "NormalUser";
    final String EMAIL = "hello3424@163.com";

    //create an account if username/email are in use
    @SmallTest
    public void testEmailUsernameNotInUse() {

        final Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(NewestObservationsActivity.class.getName(), null, false);

        final Button registerButton = (Button) getActivity().findViewById(R.id.btnRegister_RegisterActivity);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();

                ((CheckBox) getActivity().findViewById(R.id.chkbxAgreeTerm_RegisterActivity)).performClick();
                ((TextView) getActivity().findViewById(R.id.txtUserName_RegisterActivity)).setText("askldfjaslokdfj" +  (rand.nextInt(1000000) + 100));
                ((TextView) getActivity().findViewById(R.id.txtEmail_RegisterActivity)).setText("e8dbf398" + (rand.nextInt(1000000) + 100)+ "@a129384htf.com");
                registerButton.requestFocus();
                registerButton.performClick();




            }
        });

        //waiting, probably not the best way
        getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);

        TextView textView = (TextView) getActivity().findViewById(R.id.txtMessage_RegisterActivity);

        //text message popped up stating that it was successful
        assertTrue(textView.getText().length() > 0);

    }



    //don't create an account if username/email are in use
    @SmallTest
    public void testEmailUsernameInUse() {

        final Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(NewestObservationsActivity.class.getName(), null, false);

        final Button registerButton = (Button) getActivity().findViewById(R.id.btnRegister_RegisterActivity);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();

                ((CheckBox) getActivity().findViewById(R.id.chkbxAgreeTerm_RegisterActivity)).performClick();
                ((TextView) getActivity().findViewById(R.id.txtUserName_RegisterActivity)).setText(USERNAME);
                ((TextView) getActivity().findViewById(R.id.txtEmail_RegisterActivity)).setText(EMAIL);
                registerButton.requestFocus();
                registerButton.performClick();




            }
        });

        //waiting, probably not the best way
        getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);

        TextView textView = (TextView) getActivity().findViewById(R.id.txtMessage_RegisterActivity);

        //text message popped up stating that it was successful
        assertTrue(textView.getText().length() == 0);


    }

}
