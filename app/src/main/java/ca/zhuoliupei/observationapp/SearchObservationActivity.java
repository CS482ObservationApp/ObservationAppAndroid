package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Date;

import Adapter.RecordAutoCompleteAdapter;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import Interface.DatePickerCaller;
import Model.SerializableNameValuePair;
import ViewAndFragmentClass.DatePickerFragment;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class SearchObservationActivity extends AppCompatActivity implements DatePickerCaller {

    //Http params const
    private final String FIELD_CATEGORY="field_category_value";
    private final String FIELD_RECORD="field_climate_diary_record_target_id";
    private final String FIELD_RADIUS="field_geofield_distance[distance]";
    private final String FIELD_LAT="field_geofield_distance[origin][lat]";
    private final String FIELD_LNG="field_geofield_distance[origin][lon]";
    private final String FIELD_MIN_YEAR="field_date_observed_value[min][year]";
    private final String FIELD_MIN_MONTH="field_date_observed_value[min][month]";
    private final String FIELD_MIN_DAY="field_date_observed_value[min][day]";
    private final String FIELD_MAX_YEAR="field_date_observed_value[max][year]";
    private final String FIELD_MAX_MONTH="field_date_observed_value[max][month]";
    private final String FIELD_MAX_DAY="field_date_observed_value[max][day]";

    //User input variables
    private String category;
    private Date fromDate, toDate;
    private int recordID;
    private LatLng locationLatLng;
    private double rangeRadius;
    private final int INVALID = -1;
    private String address;
    private final double DEFAULT_RADIUS = 100;
    boolean recordSelected=true; //Flag to detect if the user pick an item from the autocomplete list, true by default because it could be empty

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

    //Http services object
    String baseUrl,endpoint;
    DrupalServicesView drupalServicesView;
    DrupalAuthSession drupalAuthSession;

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

        baseUrl= getText(R.string.drupal_site_url).toString();
        endpoint=getText(R.string.drupal_server_endpoint).toString();
        drupalServicesView=new DrupalServicesView(baseUrl,endpoint);
        drupalAuthSession=new DrupalAuthSession();
        drupalAuthSession.setSession(PreferenceUtil.getCookie(this));
        drupalServicesView.setAuth(drupalAuthSession);
    }

    private void initializeUI() {
        initializeToolBar();
        initializeCategorySpinner();
        initializeRecordAutoComplete();
    }

    private void setWidgetListeners() {
        setLocationTextOnTouch();
        setToDateTextOnTouch();
        setFromDateTextOnTouch();
        setTxtRecordOnTextChanged();
        setRecordItemOnClick();
        setResetButtonOnClick();
        setSearchButtonOncClick();
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
    private void setLocationTextOnTouch() {
        locationEditText.setOnTouchListener(new View.OnTouchListener() {
                                                @Override
                                                public boolean onTouch(View v, MotionEvent event) {
                                                    if (MotionEvent.ACTION_UP == event.getAction()) {
                                                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                                                        try {
                                                            startActivityForResult(builder.build(SearchObservationActivity.this), PICK_ENTRY_LOCATION_REQUEST);
                                                        } catch (Exception ex) {
                                                            Toast.makeText(v.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    return false;
                                                }
                                            }

        );
        }

    private void setTxtRecordOnTextChanged(){
        recordTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //if recordID text is empty after changed, it's fine
                if (recordTxt.getText() == null || recordTxt.getText().toString().trim().isEmpty()) {
                    recordSelected = true;
                } else {
                    recordSelected = false;
                }
            }
        });
    }
    private void setFromDateTextOnTouch() {
        fromDateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    clickedDateEditText = fromDateEditText;
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                }
                return false;
            }
        });
    }

    private void setToDateTextOnTouch() {
        toDateEditText.setOnTouchListener(new View.OnTouchListener() {
                                              @Override
                                              public boolean onTouch(View v, MotionEvent event) {
                                                  if (MotionEvent.ACTION_UP == event.getAction()) {
                                                      clickedDateEditText = toDateEditText;
                                                      DialogFragment newFragment = new DatePickerFragment();
                                                      newFragment.show(getSupportFragmentManager(), "datePicker");
                                                  }
                                                  return false;
                                              }
                                          }
            );
        }

    private void setRecordItemOnClick(){
        recordTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String record = (String) adapterView.getItemAtPosition(position);
                String nid = autoCompleteAdapter.getItemNodeID(position);
                recordID=Integer.parseInt(nid);
                recordTxt.setText(record);
                recordSelected = true;
            }
        });
    }
    private void setResetButtonOnClick(){
        findViewById(R.id.btnReset_SearchObservationActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear views
                categorySpinner.setSelection(0);
                recordTxt.setText("");
                locationEditText.setText("");
                fromDateEditText.setText("");
                toDateEditText.setText("");
                //Clear value stored in variables
                category = null;
                recordID = INVALID;
                rangeRadius = INVALID;
                locationLatLng = null;
                fromDate = null;
                toDate = null;
                recordSelected = true;
            }
        });
    }
    private void setSearchButtonOncClick(){
        findViewById(R.id.btnSearch_SearchObservationActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputOnClient()){
                    fillVariableFromInput();
                    ArrayList<SerializableNameValuePair> params=buildHttpParams();
                    Intent intent=new Intent(SearchObservationActivity.this,SearchResultActivity.class);
                    Bundle extra = new Bundle();
                    extra.putSerializable("params",  params);// cast to array and put
                    intent .putExtras(extra);
                    startActivity(intent);
                }
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

    //Helper Methods
    private boolean validateInputOnClient(){
        //If recordID edittext not empty but user did not select any autocomplete recordID, show error
        boolean recordTxtNotEmpty=recordTxt.getText()!=null&&!recordTxt.getText().toString().trim().isEmpty();
        if (recordTxtNotEmpty&&!recordSelected){
            recordTxt.setError(getString(R.string.record_not_exist_searchObservationActivity));
            recordTxt.requestFocus();
            return  false;
        }

        //If the from date is larger than to date, show error
        boolean fromDateTextNotEmpty=fromDateEditText.getText()!=null&&!fromDateEditText.getText().toString().trim().isEmpty();
        boolean toDateTextNotEmpty=toDateEditText.getText()!=null&&!toDateEditText.getText().toString().trim().isEmpty();
        if (fromDateTextNotEmpty&&toDateTextNotEmpty){
            if (fromDate.compareTo(toDate)<0){
                fromDateEditText.setError(getString(R.string.start_date_larger_than_end_searchObservationActivity));
                fromDateEditText.requestFocus();
                return false;
            }
        }


        return true;
    }
    private void fillVariableFromInput(){
        // LatLng,search radius, fromDate and toDate are filled in handlePickLocationRequestResult() handleSelectRadiusRequestResult() and handleDatePickerSetData()
        // So we only need to set recordID and category here
        if (categorySpinner.getSelectedItemPosition()>=0){
            category = (String)categorySpinner.getSelectedItem();
        }
    }
    private ArrayList<SerializableNameValuePair> buildHttpParams(){
        ArrayList<SerializableNameValuePair> pairs=new ArrayList<>();
        if (category!=null&&!category.trim().isEmpty()){
            pairs.add(new SerializableNameValuePair(FIELD_CATEGORY,category));
        }
        if (recordID !=INVALID){
            pairs.add(new SerializableNameValuePair(FIELD_RECORD, String.valueOf(recordID)));
        }
        if (locationLatLng!=null){
            pairs.add(new SerializableNameValuePair(FIELD_LAT,String.valueOf(locationLatLng.latitude)));
            pairs.add(new SerializableNameValuePair(FIELD_LNG,String.valueOf(locationLatLng.longitude)));
            pairs.add(new SerializableNameValuePair(FIELD_RADIUS,String.valueOf(rangeRadius)));
        }
        if (toDate!=null){
            pairs.add(new SerializableNameValuePair(FIELD_MAX_YEAR,String.valueOf(toDate.getYear())));
            pairs.add(new SerializableNameValuePair(FIELD_MAX_MONTH,String.valueOf(toDate.getMonth()+1)));//Server accept month from 1-12 while getMonth() return 0-11
            pairs.add(new SerializableNameValuePair(FIELD_MAX_DAY,String.valueOf(toDate.getDate())));
        }
        if (fromDate!=null){
            pairs.add(new SerializableNameValuePair(FIELD_MIN_YEAR,String.valueOf(fromDate.getYear())));
            pairs.add(new SerializableNameValuePair(FIELD_MIN_MONTH,String.valueOf(fromDate.getMonth()+1)));//Server accept month from 1-12 while getMonth() return 0-11
            pairs.add(new SerializableNameValuePair(FIELD_MIN_DAY,String.valueOf(fromDate.getDate())));
        }
        return pairs;
    }
}
