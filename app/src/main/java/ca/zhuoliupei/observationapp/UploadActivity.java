package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import Adapter.RecordAutoCompleteAdapter;
import HelperClass.PhotoUtil;
import Interface.DatePickerCaller;
import Interface.TimePickerCaller;
import ViewAndFragmentClass.DatePickerFragment;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class UploadActivity extends AppCompatActivity implements TimePickerCaller, DatePickerCaller {
    public static final int PICK_ENTRY_LCATION_REQUEST =0;
    public static final int PICK_PHOTO_REQUEST =1;
    private final int THRESHOLD=3;
    DelayAutoCompleteTextView txtRecord;

    //Variables to store the upload content
    String record,name,description;
    Bitmap selectedPicture;
    int year,month,day;
    int hour,minute;
    LatLng locationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initializeVariables();
        initializeView();
        setWidgetListeners();
    }

    private void initializeVariables(){
        record="";
        name="";
        description="";
        selectedPicture=null;
        year=-1;
        month=-1;
        day = -1;
        hour=-1;
        minute=-1;
        locationLatLng=null;
    }
    private void initializeView(){
        txtRecord = (DelayAutoCompleteTextView) findViewById(R.id.txtRecord_UploadActivity);
        txtRecord.setThreshold(THRESHOLD);
        txtRecord.setAdapter(new RecordAutoCompleteAdapter(this));
        txtRecord.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
    }
    private void setWidgetListeners(){
        setImageViewOnClick();
        setDateTimeTextOnClick();
        setRecordItemOnClick();
        setLocationTextOnClick();
        setSubmitBtnOnClick();
    }

    //Widgets Listeners,wrapped in setWidgetListeners()
    private void setImageViewOnClick(){
        ImageView imageView=(ImageView)findViewById(R.id.img_photo_uploadActivity);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtil.launchGalleryAppForPhoto((Activity) v.getContext(), PICK_PHOTO_REQUEST);
            }
        });
    }
    private void setDateTimeTextOnClick(){
        EditText editText=(EditText)findViewById(R.id.txtDateTime_uploadActivity);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }
    private void setLocationTextOnClick(){
        findViewById(R.id.txt_location_uploadActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(UploadActivity.this), PICK_ENTRY_LCATION_REQUEST);
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setRecordItemOnClick(){
        txtRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String record = (String) adapterView.getItemAtPosition(position);
                txtRecord.setText(record);
            }
        });
    }
    private void setSubmitBtnOnClick(){
        findViewById(R.id.btnSubmit_UploadActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputOnClient()) {

                }
            }
        });
    }

    private boolean validateInputOnClient(){
        //todo
        return false;
    }
    private HashMap<String,String> uploadNewObservation(){
        return new HashMap<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_PHOTO_REQUEST: handleGetPhotoRequestResult(resultCode,data);break;
            case PICK_ENTRY_LCATION_REQUEST: handlePickLocationRequestResult(resultCode,data);break;
        }
    }

    //Activity request result handlers
    private void handleGetPhotoRequestResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (data == null)
                return;
            Bitmap photo;
            //If photo comes from camera,use this to get bitmap
            Bundle bundle = data.getExtras();
            if (bundle != null)
                photo = (Bitmap) bundle.get("data");
                //If photo comes from gallary,photo would be null,use the helper method
            else
                photo = PhotoUtil.getBitmapFromUri(data.getData(),this);
            this.selectedPicture=photo;
        }
    }
    private void handlePickLocationRequestResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            LatLng latLng = place.getLatLng();
            this.locationLatLng=latLng;
        }
    }
    //Date picker interface implementation method
    @Override
    public void handleDatePickerSetData(DatePicker view, int year, int month, int day) {
        this.year=year;
        this.month=month;
        this.day=day;
    }
    @Override
    public void handleTimePickerSetData(TimePicker view, int hourOfDay, int minute) {
        this.hour=hourOfDay;
        this.minute=minute;
        TextView textView=((TextView)findViewById(R.id.txtDateTime_uploadActivity));
        textView .setText(new StringBuilder().append(year).append("-").append(month).append("-").append(day).append("  ").append(hourOfDay).append(":").append(minute).toString());
    }
    @Override
    public void handleDatePickerCancelled() {}
    @Override
    public void handleTimePickerCancelled() {
        //Cancel all the value set in handleDatePickerSetData()
        this.year=-1;
        this.month=-1;
        this.day=-1;
    }
}
