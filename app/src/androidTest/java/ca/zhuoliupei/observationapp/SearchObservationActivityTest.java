package ca.zhuoliupei.observationapp;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.Calendar;

import Adapter.RecordAutoCompleteAdapter;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class SearchObservationActivityTest extends ActivityInstrumentationTestCase2<SearchObservationActivity> {


    public SearchObservationActivityTest() {
        super(SearchObservationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    //requires 2 characters for autocomplete
    final String RECORD_AUTOCOMPLETE = "Am";

    Calendar myCalendar = Calendar.getInstance();


    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        }

    };


    //Successfully launch a search
    @SmallTest
    public void testSearchSuccessfullyLaunched()
    {



        final Spinner category = (Spinner) getActivity().findViewById(R.id.spinnerCategory_SearchObservationActivity);

        final Button submit = (Button) getActivity().findViewById(R.id.btnSearch_SearchObservationActivity);
        final EditText startDateEditText = (EditText) getActivity().findViewById(R.id.txtStartDate_SearchObservationActivity);
        final EditText latLongText = (EditText) getActivity().findViewById(R.id.txtLocation_SearchObservationActivity);


        final Instrumentation.ActivityMonitor waitActivity = getInstrumentation().addMonitor(UploadActivity.class.getName(), null, false);


        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run()  {

                category.requestFocusFromTouch();
                category.setSelection(1);
                category.performItemClick(category.getAdapter().getView(1, null, null), 1, 1);


               DatePickerDialog d = new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                d.show();

                startDateEditText.setText(String.format("%d-%02d-%02d", myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH) + 1, myCalendar.get(Calendar.DAY_OF_MONTH)));
                d.hide();
                latLongText.setText(String.format("Lat/Lng:%s,%s(%s km)", String.format("%.2f", 30.0f), String.format("%.2f", 30.0f), 10.0f));



                submit.performClick();



            }
        });

        Instrumentation.ActivityMonitor resultActivityMonitor = getInstrumentation().addMonitor(SearchResultActivity.class.getName(), null, false);

        final Activity resultAct = getInstrumentation().waitForMonitorWithTimeout(resultActivityMonitor, 5000);

        //made it to SearchResultActivity, so searching works
        assertNotNull(resultAct);
    }


    //Check whether the record field will autocomplete
    @SmallTest
    public void testRecordFieldAutocomplete()
    {
        final DelayAutoCompleteTextView recordText = (DelayAutoCompleteTextView) getActivity().findViewById(R.id.txtRecord_SearchObservationActivity);



        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run()  {
                recordText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(recordText, InputMethodManager.SHOW_IMPLICIT);
                recordText.setText(RECORD_AUTOCOMPLETE);

            }
        });

        //wait for autocomplete
        Instrumentation.ActivityMonitor resultActivityMonitor = getInstrumentation().addMonitor(RegisterActivity.class.getName(), null, false);
        final Activity resultAct = getInstrumentation().waitForMonitorWithTimeout(resultActivityMonitor, 5000);

        //If the autocomplete field has results, its functioning
        assertTrue(((RecordAutoCompleteAdapter) ((DelayAutoCompleteTextView) getActivity().findViewById(R.id.txtRecord_SearchObservationActivity)).getAdapter()).getCount() > 0);




    }


}