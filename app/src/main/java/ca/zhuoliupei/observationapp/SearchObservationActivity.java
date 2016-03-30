package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import Adapter.RecordAutoCompleteAdapter;
import HelperClass.ToolBarStyler;
import Interface.DatePickerCaller;
import ViewAndFragmentClass.DatePickerFragment;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class SearchObservationActivity extends AppCompatActivity implements DatePickerCaller {

    //User input variables
    private String category;
    private Date fromDate, toDate;
    private int recordID;
    private LatLng locationLatLng;
    private double rangeRadius;
    private final int INVALID = -1;
    private String address;
    private final double DEFAULT_RADIUS = 100;
    //Views
    Toolbar toolbar;
    Spinner categorySpinner;
    DelayAutoCompleteTextView recordTxt;
    EditText locationEditText;
    EditText fromDateEditText, toDateEditText;
    EditText clickedDateEditText;

    //Autocomplete field variables
    private final int THRESHOLD = 2;//Autocomplete only when user input more than 2 chars
    private RecordAutoCompleteAdapter autoCompleteAdapter;

    // Start Activity Request
    private final int PICK_ENTRY_LOCATION_REQUEST = 0;
    private final int SELECT_RADIUS_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_observation);
        initializeVariables();
        initializeUI();
        setWidgetListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_ENTRY_LOCATION_REQUEST:
                handlePickLocationRequestResult(resultCode, data);
                break;
            case SELECT_RADIUS_REQUEST:
                handleSelectRadiusRequestResult(resultCode, data);
                break;
        }
    }

    @Override
    public void handleDatePickerSetData(DatePicker view, int year, int month, int day) {
        if (fromDateEditText != null && toDateEditText != null) {
            if (clickedDateEditText.equals(fromDateEditText)) {
                fromDate = new Date(year, month, day);
                fromDateEditText.setText(String.format("%d-%02d-%02d", year, month+1, day));
            }
            if (clickedDateEditText.equals(toDateEditText)) {
                toDate = new Date(year, month, day);
                toDateEditText.setText(String.format("%d-%02d-%02d", year, month+1, day));
            }
        }
    }

    @Override
    public void handleDatePickerCancelled() {
        //Do nothing....
    }

    private void initializeVariables() {
        category = "";
        address = "";
        recordID = INVALID;
        rangeRadius = INVALID;

        toolbar = (Toolbar) findViewById(R.id.toolbar_SearchObservationActivity);
        categorySpinner = (Spinner) findViewById(R.id.spinnerCategory_SearchObservationActivity);
        recordTxt = (DelayAutoCompleteTextView) findViewById(R.id.txtRecord_SearchObservationActivity);
        locationEditText = (EditText) findViewById(R.id.txtLocation_SearchObservationActivity);
        fromDateEditText = (EditText) findViewById(R.id.txtFromDate_SearchObservationActivity);
        toDateEditText = (EditText) findViewById(R.id.txtToDate_SearchObservationActivity);

        autoCompleteAdapter = new RecordAutoCompleteAdapter(this);

    }

    private void initializeUI() {
        initializeToolBar();
        initializeCategorySpinner();
        initializeRecordAutoComplete();
    }

    private void setWidgetListeners() {
        setLocationTextOnClick();
        setToDateTextOnClick();
        setFromDateTextOnClick();
    }


    /******
     * Wrapped in initializeUI()
     ************/
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

    /**********
     * Wrapped in setWidgetListeners()
     **************/
    private void setLocationTextOnClick() {
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(SearchObservationActivity.this), PICK_ENTRY_LOCATION_REQUEST);
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFromDateTextOnClick() {
        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedDateEditText = fromDateEditText;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void setToDateTextOnClick() {
        toDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedDateEditText = toDateEditText;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    /******************** Start Activity Result Handler***************************/
    private void handlePickLocationRequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            LatLng latLng = place.getLatLng();
            this.locationLatLng = latLng;
            if (place.getAddress() != null)
                address = place.getAddress().toString();
            Intent intent = new Intent(SearchObservationActivity.this, ChangeTextFieldActivity.class);
            intent.putExtra(ChangeTextFieldActivity.INTENT_TYPE, ChangeTextFieldActivity.Type.AREA_RADIUS);
            startActivityForResult(intent, SELECT_RADIUS_REQUEST);
        }
    }

    private void handleSelectRadiusRequestResult(int resultCode, Intent data) {
        String areaStr = "";
        if (resultCode == RESULT_OK) {
            try {
                rangeRadius = Double.parseDouble(data.getExtras().getString(ChangeTextFieldActivity.INTENT_RESULT));
            } catch (Exception ex) {
                rangeRadius = DEFAULT_RADIUS;
            }
        } else {
            rangeRadius = DEFAULT_RADIUS;
        }
        if (address != null && !address.trim().isEmpty()) {
            areaStr += String.format("%s(%s km)", address, rangeRadius);
        } else if (locationLatLng != null) {
            areaStr += String.format("Lat/Lng:%s,%s(%s km)", String.format("%.2f", locationLatLng.latitude), String.format("%.2f", locationLatLng.longitude), rangeRadius);
        } else {
            areaStr = "";
        }
        locationEditText.setText(areaStr);
    }
}
