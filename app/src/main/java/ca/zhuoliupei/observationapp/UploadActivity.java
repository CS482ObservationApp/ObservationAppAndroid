package ca.zhuoliupei.observationapp;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import Adapter.RecordAutoCompleteAdapter;
import Const.AppConst;
import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesFile;
import DrupalForAndroidSDK.DrupalServicesNode;
import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.GeoUtil;
import HelperClass.NotificationUtil;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import HelperClass.RegexValidator;
import HelperClass.UploadUtil;
import Interface.DatePickerCaller;
import Interface.TimePickerCaller;
import ViewAndFragmentClass.DatePickerFragment;
import ViewAndFragmentClass.DelayAutoCompleteTextView;

public class UploadActivity extends AppCompatActivity implements  DatePickerCaller {
    public static final String FID="fid";
    public static final String RECORD="field_climate_diary_record";
    public static final String NAME = "title_field";
    public static final String DATE = "field_date_observed";
    public static final String LATLON ="field_location_lat_long";
    public static final String DESCRIPTION = "field_comments";
    public static final String ADDRESS = "field_location_observed";
    public static final String IMAGE = "field_image";
    public static final String CONTENT_TYPE="type";

    public static final int INVALID=-1;
    public static final int CANCEL_UPLOAD=-1;
    public static final int PICK_ENTRY_LCATION_REQUEST =0;
    public static final int PICK_PHOTO_REQUEST =1;
    public static final int  CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST=2;
    public static final int MAX_UPLOAD_SIZE=1600*1600;
    private final int THRESHOLD=3;
    DelayAutoCompleteTextView txtRecord;

    ReverseGeoCodeTask reverseGeoCodeTask;
    UploadObservationTask uploadObservationTask;

    DrupalServicesFile drupalServicesFile;
    DrupalAuthSession drupalAuthSession;
    DrupalServicesNode drupalServicesNode;
    String baseUrl,endpoint;

    //Variables to store the upload content
    String record,name,description;
    Bitmap selectedPicture;
    int year,month,day;
    LatLng locationLatLng;
    Address selectedAddress;
    int imageFileID=INVALID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initializeVariables();
        initializeView();
        setWidgetListeners();
    }

    @Override
    public void onBackPressed() {
        showConfirmCancelUploadDialog(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reverseGeoCodeTask!=null)
            reverseGeoCodeTask.cancel(true);
    }

    private void initializeVariables(){
        record="";
        name="";
        description="";
        selectedPicture=null;
        year=INVALID;
        month=INVALID;
        day = INVALID;
        locationLatLng=null;
        baseUrl= getText(R.string.drupal_site_url).toString();
        endpoint=getText(R.string.drupal_server_endpoint).toString();

        drupalServicesFile=new DrupalServicesFile(baseUrl,endpoint);
        drupalServicesNode=new DrupalServicesNode(baseUrl,endpoint);
        drupalAuthSession=new DrupalAuthSession();

        drupalAuthSession.setSession(PreferenceUtil.getCookie(this));
        drupalServicesFile.setAuth(drupalAuthSession);
        drupalServicesNode.setAuth(drupalAuthSession);
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
        setCancelBtnOnClick();
    }

    //Widgets Listeners,wrapped in setWidgetListeners()
    private void setImageViewOnClick(){
        findViewById(R.id.img_photo_uploadActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChooseUploadPhotoMethodActivity.class), CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST);
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
                    NotificationUtil.showNotification(v.getContext(),AppConst.UPLOADING_NOTIFICATION_ID);
                    uploadObservationTask = new UploadObservationTask(v.getContext());
                    uploadObservationTask.execute();
                    finish();
                }
            }
        });
    }
    private void setCancelBtnOnClick(){
        findViewById(R.id.btnCancel_UploadActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmCancelUploadDialog(v.getContext());
            }
        });
    }


    //Handle activity request result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST: handleChooseUploadMethodRequestResult(resultCode, data); break;
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

            this.selectedPicture=PhotoUtil.reduceBitMapSize(photo,MAX_UPLOAD_SIZE);
            ((ImageView)findViewById(R.id.img_photo_uploadActivity)).setImageBitmap(photo);
        }
    }
    private void handlePickLocationRequestResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            LatLng latLng = place.getLatLng();
            this.locationLatLng=latLng;
            reverseGeoCodeTask = new ReverseGeoCodeTask(this);
            reverseGeoCodeTask.execute(latLng);
        }
    }
    private void handleChooseUploadMethodRequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            int result = data.getIntExtra("result", CANCEL_UPLOAD);
            PhotoUtil.startPickingPhoto(this, result, PICK_PHOTO_REQUEST);
        }
    }


    private class ReverseGeoCodeTask extends AsyncTask<LatLng, Void, Address> {
        Context context;
        LatLng latLng;
        public ReverseGeoCodeTask(Context context) {
            this.context = context;
        }
        @Override
        protected Address doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            latLng = params[0];
            double lat = latLng.latitude;
            double lon = latLng.longitude;
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses == null || addresses.size() == 0) {
                    return null;
                }
                return addresses.get(0);
            } catch (Exception ex) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(final Address address) {
            if (address == null) {
                //Show Dialog to select continue or not
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.address_not_found_continue_upload_msg).setTitle(R.string.address_not_found_title);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            ((TextView)findViewById(R.id.txt_location_uploadActivity)).setText(constructLatLonString(locationLatLng));
                        } catch (Exception ex) {
                            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {/*Do nothing...*/}
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                selectedAddress=address;
                ((TextView)findViewById(R.id.txt_location_uploadActivity)).setText(GeoUtil.getFullAddress(address));
            }
        }
    }
    private class UploadObservationTask extends AsyncTask<Void,Void,Void>{
        Context context;
        public UploadObservationTask(Context context){
            this.context=context;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, String> resultMap = null;
            try {
                if (selectedPicture != null) {
                    imageFileID = uploadImageFile(drupalServicesFile, selectedPicture);
                    if (imageFileID == INVALID) {
                        throw new Exception();
                    }
                }
                fillVariablesFromInput();
                BasicNameValuePair[] params = constructHttpParams();
                resultMap = drupalServicesNode.create(params);
            } catch (Exception ex) {}

            //Remove the uploading notification
            NotificationUtil.removeNotification(context,AppConst.UPLOADING_NOTIFICATION_ID);
            if (resultMap==null||!resultMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
                //Show the Upload Failed Notification
                NotificationUtil.showNotification(context,AppConst.UPLOAD_FAILED_NOTIFICATION_ID);
            }
            return null;
        }
    }


    //Helper Methods
    private boolean validateInputOnClient(){
        EditText nameTextBox=(EditText)findViewById(R.id.txtName_UploadActivity);
        String name=nameTextBox.getText().toString();
        if ( name.isEmpty()){
            nameTextBox.setError(getText(R.string.field_empty_error));
            return false;
        }
        EditText dateTimeTextBox=(EditText)findViewById(R.id.txtDateTime_uploadActivity);
        String datetime=dateTimeTextBox.getText().toString();
        if (datetime.isEmpty()){
            dateTimeTextBox.setError(getText(R.string.field_empty_error));
            return false;
        }
        EditText locationTextBox=(EditText)findViewById(R.id.txt_location_uploadActivity);
        String location=locationTextBox.getText().toString();
        if (locationLatLng==null||location.isEmpty()){
            locationTextBox.setError(getText(R.string.field_empty_error));
        }
        return true;
    }
    private String constructLatLonString(LatLng latLng){
        if (latLng!=null)
            return String.format("Lat/Lon: %s,%s", latLng.latitude, latLng.longitude);
        else return "";
    }
    private void fillVariablesFromInput(){
        record=((TextView)findViewById(R.id.txtRecord_UploadActivity)).getText().toString();
        name=((TextView)findViewById(R.id.txtName_UploadActivity)).getText().toString();
        description=((TextView)findViewById(R.id.txtDescription_UploadActivity)).getText().toString();
    }
    private void showConfirmCancelUploadDialog(Context context){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.cancel_upload_msg)
                        .setTitle(R.string.cancel_upload_title);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {/*Do nothing*/}
                });
                AlertDialog dialog = builder.create();
                dialog.show();
    }
    private int uploadImageFile(DrupalServicesFile drupalServicesFile,Bitmap bitmap) throws Exception{
        String filename = "observation_entry_image_" + PreferenceUtil.getCurrentUser(this) + "_" + System.currentTimeMillis() + ".jpg";
        String fileServerPath = "public://" + filename;
        String encoded = UploadUtil.getBase64StringFromBitmap(bitmap);
        BasicNameValuePair[] params = UploadUtil.constructBasicFileUploadParams(filename, fileServerPath, encoded);
        HashMap<String, String> createFileResponse = drupalServicesFile.create(params);
        if (createFileResponse.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals("200")) {
            JSONObject responseObject = new JSONObject(createFileResponse.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
            int fid = responseObject.getInt(FID);
            return fid;
        }
        return INVALID;
    }

    private BasicNameValuePair[] constructHttpParams(){
        ArrayList<BasicNameValuePair> paramsList=new ArrayList<>();
        paramsList.add(new BasicNameValuePair(CONTENT_TYPE, "climate_diary_entry"));
        if (record!=null&&!record.isEmpty()){
            paramsList.add(new BasicNameValuePair(RECORD,record));
        }
        if (description!=null && !description.isEmpty()){
            paramsList.add(new BasicNameValuePair(DESCRIPTION,description));
        }
        if (name!=null && !name.isEmpty()){
            paramsList.add(new BasicNameValuePair(NAME+"[und][0][value]",name));
        }
        if (year!=-1 && month!=-1&& day!=-1){
            paramsList.add(new BasicNameValuePair(DATE+"[und][0][value][year]",String.valueOf(year) ));
            paramsList.add(new BasicNameValuePair(DATE+"[und][0][value][month]",String.valueOf(month) ));
            paramsList.add(new BasicNameValuePair(DATE+"[und][0][value][day]",String.valueOf(day) ));
        }
        if (locationLatLng!=null){
            paramsList.add(new BasicNameValuePair(LATLON+"[und][0][geom][lat]",String.valueOf(locationLatLng.latitude )));
            paramsList.add(new BasicNameValuePair(LATLON+"[und][0][geom][lon]",String.valueOf(locationLatLng.longitude )));
        }
        if (selectedAddress!=null){
            paramsList.add(new BasicNameValuePair(ADDRESS+"[und][0][country]",selectedAddress.getCountryCode()));
            paramsList.add(new BasicNameValuePair(ADDRESS+"[und][0][locality]",selectedAddress.getLocality()));
            paramsList.add(new BasicNameValuePair(ADDRESS+"[und][0][thoroughfare]",selectedAddress.getThoroughfare()));
            paramsList.add(new BasicNameValuePair(ADDRESS+"[und][0][postal_code]",selectedAddress.getPostalCode()));
        }
        if (imageFileID!=INVALID){
            paramsList.add(new BasicNameValuePair(IMAGE+"[und][0][fid]", String.valueOf(imageFileID)));
        }
        return paramsList.toArray(new BasicNameValuePair[paramsList.size()]);
    }


    //Date picker interface implementation method
    @Override
    public void handleDatePickerSetData(DatePicker view, int year, int month, int day) {
        this.year=year;
        this.month=month+1;//Jan is month 0
        this.day=day;
        TextView textView=((TextView)findViewById(R.id.txtDateTime_uploadActivity));
        textView .setText(String.format("%s-%d-%d", String.valueOf(year), month+1, day));
        textView.setError(null);
    }

    @Override
    public void handleDatePickerCancelled() {/*Do nothing....*/}

}
