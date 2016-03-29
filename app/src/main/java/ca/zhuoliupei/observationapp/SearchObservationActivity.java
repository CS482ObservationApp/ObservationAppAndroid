package ca.zhuoliupei.observationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import Adapter.RecordAutoCompleteAdapter;
import HelperClass.ToolBarStyler;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class SearchObservationActivity extends AppCompatActivity {

    //User input variables
    private String category;
    private String fromDate, toDate;
    private int recordID;
    private double lat, lon;
    private double rangeRadius;
    private final int INVALID = -1;

    //Views
    Toolbar toolbar;
    Spinner categorySpinner;
    DelayAutoCompleteTextView recordTxt;
    EditText locationEditText;
    EditText fromDateEditText, toDateEditText;

    //Autocomplete field variables
    private final int THRESHOLD = 2;//Autocomplete only when user input more than 2 chars
    private RecordAutoCompleteAdapter autoCompleteAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_observation);
        initializeVariables();
        initializeUI();
        setWidgetListeners();
    }

    private void initializeVariables() {
        category = "";
        fromDate = "";
        toDate = "";
        recordID = INVALID;
        lat = INVALID;
        lon = INVALID;
        rangeRadius = INVALID;

        toolbar = (Toolbar) findViewById(R.id.toolbar_SearchObservationActivity);
        categorySpinner = (Spinner) findViewById(R.id.spinnerCategory_SearchObservationActivity);
        locationEditText = (EditText) findViewById(R.id.txtLocation_SearchObservationActivity);
        fromDateEditText = (EditText) findViewById(R.id.txtFromDate_SearchObservationActivity);
        toDateEditText = (EditText) findViewById(R.id.txtToDate_SearchObservationActivity);

        autoCompleteAdapter=new RecordAutoCompleteAdapter(this);

    }

    private void initializeUI() {
        initializeToolBar();
        initializeCategorySpinner();
        initializeRecordAutoComplete();
    }

    private void setWidgetListeners() {

    }


    private void initializeToolBar() {
        ToolBarStyler.styleToolBar(this, toolbar, getString(R.string.search_tool_bar_title_searchObservationActivity));
    }

    private void initializeCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void initializeRecordAutoComplete() {
        recordTxt.setThreshold(THRESHOLD);
        recordTxt.setAdapter(autoCompleteAdapter);
        recordTxt.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator_SearchObservationActivity));
        recordTxt.setTag(-1);//Initial tag which would be invalid to upload
    }
}
