package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.Test;

import java.util.Random;

import Adapter.RecordAutoCompleteAdapter;
import ViewAndFragmentClass.DatePickerFragment;
import ViewAndFragmentClass.DelayAutoCompleteTextView;


public class UploadActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {


    public UploadActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    final String RECORD_AUTOCOMPLETE = "Am";
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

    //check to see if the UI components are available.  This checks for changes in the UI components IDs.
    @SmallTest
    public void testUIShowingUp() {


        Activity activity = loginState();


        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(UploadActivity.class.getName(), null, false);

        activity.startActivity(new Intent(activity, UploadActivity.class));

        final Activity uploadAct = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);


        DelayAutoCompleteTextView recordText = (DelayAutoCompleteTextView) uploadAct.findViewById(R.id.txtRecord_UploadActivity);
        ImageView imgView=(ImageView)uploadAct.findViewById(R.id.img_photo_uploadActivity);;
        TextView txtName=(EditText)uploadAct.findViewById(R.id.txtName_UploadActivity);
        TextView txtDateTime=(EditText)uploadAct.findViewById(R.id.txtDateTime_uploadActivity);
        TextView txtLocation=(EditText)uploadAct.findViewById(R.id.txt_location_uploadActivity);
        TextView txtDescription=(TextView)uploadAct.findViewById(R.id.txtDescription_UploadActivity);
        Button btnCancel=(Button)uploadAct.findViewById(R.id.btnCancel_UploadActivity);
        Button btnSubmit=(Button)uploadAct.findViewById(R.id.btnSubmit_UploadActivity);
        Toolbar toolbar=(Toolbar)uploadAct.findViewById(R.id.toolbar_UploadActivity);



        assertNotNull(recordText);
        assertNotNull(imgView);
        assertNotNull(txtName);
        assertNotNull(txtDateTime);
        assertNotNull(txtLocation);
        assertNotNull(txtDescription);
        assertNotNull(btnCancel);
        assertNotNull(btnSubmit);
        assertNotNull(toolbar);


        uploadAct.finish();
        activity.finish();
    }


    //Users shouldn't be able to upload a record that does not exist
    @SmallTest
    public void testInvalidRecord() {

        Activity activity = loginState();
        Log.d("wawa", "Good?: " + activity.getLocalClassName());


        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(UploadActivity.class.getName(), null, false);

        activity.startActivity(new Intent(activity, UploadActivity.class));

        final Activity uploadAct = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);
        final DelayAutoCompleteTextView recordText = (DelayAutoCompleteTextView) uploadAct.findViewById(R.id.txtRecord_UploadActivity);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {

                recordText.setText("Some fake obs");

                Button submit = (Button) uploadAct.findViewById(R.id.btnSubmit_UploadActivity);
                submit.performClick();
                assertNotNull(recordText.getError());

            }
        });

        uploadAct.finish();
        activity.finish();
    }



    //Check whether the record field will autocomplete
    @SmallTest
    public void testRecordFieldAutocomplete()
    {

        //get a valid user before going to the upload activity
        Activity activity = loginState();

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(UploadActivity.class.getName(), null, false);

        activity.startActivity(new Intent(activity, UploadActivity.class));

        final Activity uploadAct = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 10000);


        final DelayAutoCompleteTextView recordText = (DelayAutoCompleteTextView) uploadAct.findViewById(R.id.txtRecord_UploadActivity);



        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                recordText.requestFocus();
                InputMethodManager imm = (InputMethodManager) uploadAct.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(recordText, InputMethodManager.SHOW_IMPLICIT);
                recordText.setText(RECORD_AUTOCOMPLETE);

            }
        });

        //wait for autocomplete
        Instrumentation.ActivityMonitor resultActivityMonitor = getInstrumentation().addMonitor(RegisterActivity.class.getName(), null, false);
        final Activity resultAct = getInstrumentation().waitForMonitorWithTimeout(resultActivityMonitor, 6000);

         //If the autocomplete field has results, its functioning
        assertTrue(((RecordAutoCompleteAdapter) ((DelayAutoCompleteTextView) uploadAct.findViewById(R.id.txtRecord_UploadActivity)).getAdapter()).getCount() > 0);

        uploadAct.finish();
        activity.finish();

    }


}