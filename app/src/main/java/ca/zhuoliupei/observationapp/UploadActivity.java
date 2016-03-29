package ca.zhuoliupei.observationapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

import Adapter.RecordAutoCompleteAdapter;
import Const.AppConst;
import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesFile;
import DrupalForAndroidSDK.DrupalServicesNode;
import HelperClass.GeoUtil;
import HelperClass.NotificationUtil;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import HelperClass.UploadUtil;
import Interface.DatePickerCaller;
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
    private final int THRESHOLD=2;//Autocomplete only when user input more than 2 chars
    DelayAutoCompleteTextView txtRecord;
    ImageView imgView;
    TextView txtName;
    TextView txtDateTime;
    TextView txtLocation;
    TextView txtDescription;
    Button btnCancel;
    Button btnSubmit;

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
    String recordID;

    RecordAutoCompleteAdapter autoCompleteAdapter;
    boolean recordSelected=true; //Flag to detect if the user pick an item from the autocomplete list, true by default because it could be empty
    boolean imageUploaded=false;//Flag to indicate if image is uploaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initializeVariables();
        initializeUI();
        setWidgetListeners();
    }

    @Override
    public void onBackPressed() {
        if (inputFieldNotEmpty())
            showConfirmCancelUploadDialog(this);
        else
            finish();
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

        autoCompleteAdapter=new RecordAutoCompleteAdapter(this);

        txtRecord = (DelayAutoCompleteTextView) findViewById(R.id.txtRecord_UploadActivity);
        imgView=(ImageView)findViewById(R.id.img_photo_uploadActivity);
        txtDateTime=(EditText)findViewById(R.id.txtDateTime_uploadActivity);
        txtLocation=(EditText)findViewById(R.id.txt_location_uploadActivity);
        btnCancel=(Button)findViewById(R.id.btnCancel_UploadActivity);
        btnSubmit=(Button)findViewById(R.id.btnSubmit_UploadActivity);
        txtName=(EditText)findViewById(R.id.txtName_UploadActivity);
        txtDescription=(TextView)findViewById(R.id.txtDescription_UploadActivity);
    }
    private void initializeUI(){
       initializeRecordAutoComplete();
    }
    private void setWidgetListeners(){
        setImageViewOnClick();
        setDateTimeTextOnClick();
        setRecordItemOnClick();
        setLocationTextOnClick();
        setSubmitBtnOnClick();
        setCancelBtnOnClick();
        setTxtRecordOnTextChanged();
    }

    //Wrapped in initializeUI()
    private void initializeRecordAutoComplete(){
        txtRecord.setThreshold(THRESHOLD);
        txtRecord.setAdapter(autoCompleteAdapter);
        txtRecord.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator_UploadActivity));
        txtRecord.setTag(-1);//Initial tag which would be invalid to upload
    }

    //Widgets Listeners,wrapped in setWidgetListeners()
    private void setImageViewOnClick(){
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChooseUploadPhotoMethodActivity.class), CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST);
            }
        });
    }
    private void setDateTimeTextOnClick(){
        txtDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }
    private void setLocationTextOnClick(){
        txtLocation.setOnClickListener(new View.OnClickListener() {
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
                String nid = autoCompleteAdapter.getItemNodeID(position);
                txtRecord.setText(record);
                txtRecord.setTag(nid);
                recordSelected = true;
            }
        });
    }
    private void setTxtRecordOnTextChanged(){
        txtRecord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtRecord.getText()==null || txtRecord.getText().toString().trim().isEmpty()){
                    recordSelected=true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                recordSelected=false;
            }
        });
    }
    private void setSubmitBtnOnClick(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputOnClient()) {
                    NotificationUtil.showNotification(v.getContext(), AppConst.UPLOADING_NOTIFICATION_ID);
                    uploadObservationTask = new UploadObservationTask(v.getContext());
                    uploadObservationTask.execute();
                    finish();
                }
            }
        });
    }
    private void setCancelBtnOnClick(){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputFieldNotEmpty())
                    showConfirmCancelUploadDialog(v.getContext());
                else
                    finish();
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
            imgView.setImageBitmap(photo);
            imageUploaded=true;
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
                            txtLocation.setText(constructLatLonString(locationLatLng));
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
               txtLocation.setText(GeoUtil.getFullAddress(address));
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
        String name=txtName.getText().toString();
        if (!recordSelected){
            txtRecord.setError(getText(R.string.record_not_exist));
            return false;
        }
        if ( name.isEmpty()){
            txtName.setError(getText(R.string.field_empty_error));
            return false;
        }
        String datetime=txtDateTime.getText().toString();
        if (datetime.isEmpty()){
            txtDateTime.setError(getText(R.string.field_empty_error));
            return false;
        }
        String location=txtLocation.getText().toString();
        if (locationLatLng==null||location.isEmpty()){
            txtLocation.setError(getText(R.string.field_empty_error));
            return false;
        }
        return true;
    }
    private String constructLatLonString(LatLng latLng){
        if (latLng!=null)
            return String.format("Lat/Lon: %s,%s", latLng.latitude, latLng.longitude);
        else return "";
    }
    private void fillVariablesFromInput(){
        record=txtRecord.getText().toString().trim();
        recordID=(String)txtRecord.getTag();
        name=txtName.getText().toString().trim();
        description=txtDescription.getText().toString().trim();
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
    private boolean inputFieldNotEmpty(){
        if (txtRecord.getText()!=null && !txtRecord.getText().toString().isEmpty())
            return true;
        if (txtName.getText()!=null && !txtName.getText().toString().isEmpty())
            return true;
        if (txtDescription.getText()!=null && !txtDescription.getText().toString().isEmpty())
            return true;
        if (txtDateTime.getText()!=null && !txtDateTime.getText().toString().isEmpty())
            return true;
        if (txtLocation.getText()!=null && !txtLocation.getText().toString().isEmpty())
            return true;
        if (imageUploaded)
            return true;
        return false;
    }
    private BasicNameValuePair[] constructHttpParams(){
        ArrayList<BasicNameValuePair> paramsList=new ArrayList<>();
        paramsList.add(new BasicNameValuePair(CONTENT_TYPE, "climate_diary_entry"));
        if (record!=null&&!record.isEmpty()){
            paramsList.add(new BasicNameValuePair(RECORD+"[und][0][target_id]",record));
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
        txtDateTime .setText(String.format("%s-%d-%d", String.valueOf(year), month+1, day));
        txtDateTime.setError(null);
    }

    @Override
    public void handleDatePickerCancelled() {/*Do nothing....*/}
}
