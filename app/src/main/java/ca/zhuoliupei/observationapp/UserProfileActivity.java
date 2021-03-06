/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Const.SharedPreferencesConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesFile;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.GeoUtil;
import HelperClass.MyObservationCacheManager;
import HelperClass.NotificationUtil;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import HelperClass.ToolBarStyler;
import HelperClass.UploadUtil;
import HelperClass.NotificationUtil.NotificationID;

/** This class provides functions:
 * 1. Showing user profile
 * 2. Editing user profile includes image,username,address 1 and 2
 * 3. Provide entries to MyPost Activity, Reset Password Activity, Logout
 */
public class UserProfileActivity extends AppCompatActivity {
    private static final int INVALID=-1;
    private static final int CANCEL_UPLOAD=-1;
    private static final int CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST=0;
    private static final int PICK_PHOTO_REQUEST =1;
    private static final int CHANGE_USER_NAME_REQUEST=2;
    private static final int PROFILE_ADDRESS1_PICKER_REQUEST =3;
    private static final int PROFILE_ADDRESS2_PICKER_REQUEST =4;
    private static final String FILE_FILE_ID="fid";

    private final int MAX_USER_IMAGE_UPLOAD_SIZE=300*300;
    private final int MAX_USER_IMAGE_VIEW_SIZE=50*50;

    private final String USER_NAME="name";
    private final String EMAIL="mail";
    private final String PICTURE="picture";
    private final String PICTURE_URL="url";
    private final String ADDRESS1="field_l1_address";
    private final String ADDRESS2="field_l2_address";
    private final String COUNTRY="country";
    private final String LOCALITY="locality";
    private final String ADMIN_AREA="administrative_area";
    private final String THOROUGHFARE ="thoroughfare";
    private final String UND="und";

    String baseUrl, endpoint;
    DrupalAuthSession authSession;
    DrupalServicesUser drupalServicesUser;
    DrupalServicesFile drupalServicesFile;
    UpdateServerUserProfileImageTask updateServerUserProfileImageTask;
    UpdateServerUserProfileNameTask updateServerUserProfileNameTask;
    UpdateServerUserLocationTask updateServerUserLocationTask;
    DetectUserInfoChangesTask detectUserInfoChangesTask;
    LogoutUserTask logoutUserTask;
    ReverseGeoCodeTask reverseGeoCodeTask;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        initializeVariables();
        initializeUI();
        setWidgetListeners();

        beginDetectUserInfoChanges();
    }

    private void initializeVariables() {
        baseUrl = getText(R.string.drupal_site_url).toString();
        endpoint = getText(R.string.drupal_server_endpoint).toString();
        authSession = new DrupalAuthSession();
        authSession.setSession(PreferenceUtil.getCookie(this));
        drupalServicesUser = new DrupalServicesUser(baseUrl, endpoint);
        drupalServicesFile = new DrupalServicesFile(baseUrl, endpoint);
        drupalServicesUser.setAuth(authSession);
        drupalServicesFile.setAuth(authSession);
        toolbar = (Toolbar) findViewById(R.id.toolbar_UserProfileActivity);
    }

    private void initializeUI() {
        initializeView();
        initializeToolbar();
    }

    private void initializeView(){
        ImageView profileImageIV = (ImageView) findViewById(R.id.imgUserImage_UserProfileActivity);
        TextView userNameTV = (TextView) findViewById(R.id.txtUserName_UserProfileActivity);
        TextView emailTV = (TextView) findViewById(R.id.txtEmail_UserProfileActivity);
        TextView address1TV = (TextView) findViewById(R.id.txtAddress1_UserProfileActivity);
        TextView address2TV = (TextView) findViewById(R.id.txtAddress2_UserProfileActivity);

        String imgFilePath = PreferenceUtil.getCurrentUserPictureLocalUri(this);
        String userName = PreferenceUtil.getCurrentUser(this);
        String email = PreferenceUtil.getCurrentUserEmail(this);
        String address1 = PreferenceUtil.getCurrentUserLocation1(this);
        String address2 = PreferenceUtil.getCurrentUserLocation2(this);

        //Control the bitmap size to prevent OOM
        File imgFile = new File(imgFilePath);
        if (imgFile.exists())
            profileImageIV.setImageBitmap(PhotoUtil.getBitmapFromFile(imgFile,MAX_USER_IMAGE_VIEW_SIZE));
        else profileImageIV.setImageResource(R.drawable.icon_user_default);

        userNameTV.setText(userName);
        emailTV.setText(email);
        address1TV.setText(address1);
        address2TV.setText(address2);
    }
    private void initializeToolbar(){
        if (toolbar!=null){
            ToolBarStyler.styleToolBar(this,toolbar,"Profile");
        }
    }

    private void beginDetectUserInfoChanges(){
        if (detectUserInfoChangesTask!=null)
            detectUserInfoChangesTask.cancel(true);
        detectUserInfoChangesTask=new DetectUserInfoChangesTask(this);
        detectUserInfoChangesTask.execute();
    }
    /*
    * setWidgetListeners contains the other setOnClickListener Methods
    */
    private void setWidgetListeners() {
        setProfilePhotoOnClickListener();
        setUserNameOnClickListener();
        setEmailOnClickListener();
        setAddress1OnClickListener();
        setAddress2OnClickListener();
        setLogoutOnClickListener();
        setMyPostOnClickListener();
        setChangePasswordOnClick();
    }
    private void setUserNameOnClickListener() {
        findViewById(R.id.ll_name_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangeTextFieldActivity.class);
                intent.putExtra(ChangeTextFieldActivity.INTENT_TYPE, ChangeTextFieldActivity.Type.USER_NAME);
                startActivityForResult(intent, CHANGE_USER_NAME_REQUEST);
            }
        });
    }
    private void setEmailOnClickListener(){
        findViewById(R.id.ll_email_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserProfileActivity.this,R.string.cannot_change_email_userProfileActivity,Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setAddress1OnClickListener() {
        findViewById(R.id.ll_address1_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build((Activity) (v.getContext())), PROFILE_ADDRESS1_PICKER_REQUEST);
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setAddress2OnClickListener() {
        findViewById(R.id.ll_address2_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build((Activity) (v.getContext())), PROFILE_ADDRESS2_PICKER_REQUEST);
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setProfilePhotoOnClickListener() {
        findViewById(R.id.ll_photo_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChooseUploadPhotoMethodActivity.class), CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST);
            }
        });
    }
    private void setLogoutOnClickListener(){
        findViewById(R.id.ll_logout_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context=v.getContext();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.logout_msg_userProfileActivity)
                        .setTitle(R.string.logout_title_userProfileActivity);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean result=deleteLocalUserSessionInfo(context);
                        if (result){
                            logoutUserTask=new LogoutUserTask(context,drupalServicesUser);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                logoutUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } else {
                                logoutUserTask.execute();
                            }
                            //Start a whole new activity without back stack
                            Intent intent=new Intent(UserProfileActivity.this, NewestObservationsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else {
                            Toast.makeText(context,R.string.failed_logout_userProfileActivity,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {/*Do nothing*/}
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    private void setMyPostOnClickListener(){
        findViewById(R.id.ll_myObservations_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this, MyPostActivity.class));
            }
        });
    }
    private void setChangePasswordOnClick(){
        findViewById(R.id.ll_changePassword_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this,ResetPasswordActivity.class));
            }
        });
    }

    /* Handle Activity Results*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST: {
                handleChooseUploadMethodRequestResult(resultCode, data);
                break;
            }
            case PICK_PHOTO_REQUEST: {
                handleGetPhotoRequestResult(resultCode, data);
                break;
            }
            case CHANGE_USER_NAME_REQUEST: {
                handleChangeUserNameRequestResult(resultCode, data);
                break;
            }
            case PROFILE_ADDRESS1_PICKER_REQUEST: {
                handleChangeAddress1RequestResult(resultCode, data);
                break;
            }
            case PROFILE_ADDRESS2_PICKER_REQUEST: {
                handleChangeAddress2RequestResult(resultCode, data);
                break;
            }
        }
    }
    //Handle methods, called by onActivityResult()
    private void handleChooseUploadMethodRequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            int result = data.getIntExtra("result", CANCEL_UPLOAD);
            PhotoUtil.startPickingPhoto(this, result, PICK_PHOTO_REQUEST);
        }
    }
    private void handleGetPhotoRequestResult(int resultCode, Intent data) {
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
                photo = PhotoUtil.getBitmapFromLocalUri(data.getData(), this);
            photo=PhotoUtil.reduceBitMapSize(photo,MAX_USER_IMAGE_UPLOAD_SIZE);
            updateServerUserProfileImageTask=new UpdateServerUserProfileImageTask(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                updateServerUserProfileImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,photo);
            } else {
                updateServerUserProfileImageTask.execute(photo);
            }
        }
    }
    private void handleChangeUserNameRequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null)
                return;
            String newName = data.getExtras().getString("result");
            if (newName != null && !newName.isEmpty()) {
                updateServerUserProfileNameTask = new UpdateServerUserProfileNameTask(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    updateServerUserProfileNameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,newName);
                } else {
                    updateServerUserProfileNameTask.execute(newName);
                }
            }
        }
    }
    private void handleChangeAddress1RequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            LatLng latLng = place.getLatLng();
            reverseGeoCodeTask = new ReverseGeoCodeTask(this,PROFILE_ADDRESS1_PICKER_REQUEST);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                reverseGeoCodeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,latLng);
            } else {
                reverseGeoCodeTask.execute(latLng);
            }
        }
    }
    private void handleChangeAddress2RequestResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            LatLng latLng = place.getLatLng();
            reverseGeoCodeTask = new ReverseGeoCodeTask(this,PROFILE_ADDRESS2_PICKER_REQUEST);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                reverseGeoCodeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,latLng);
            } else {
                reverseGeoCodeTask.execute(latLng);
            }
        }
    }


    //AsyncTasks for upload info
    private class UpdateServerUserProfileImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        Bitmap photo;
        Context context;

        public UpdateServerUserProfileImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            photo = params[0];
            try {
                NotificationUtil.showNotification(context, NotificationID.UPDATE_USER_PROFILE_IMAGE_NOTIFICATION_ID);
                return updateServerUserProfileImage(photo);
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean updateResult) {
            NotificationUtil.removeNotification(context,NotificationID.UPDATE_USER_PROFILE_IMAGE_NOTIFICATION_ID);
            if (updateResult) {
                boolean saveResult = updateLocalUserProfileImage(photo, context);
                if (saveResult) {
                    ((ImageView) findViewById(R.id.imgUserImage_UserProfileActivity)).setImageBitmap(photo);
                    Toast.makeText(context, R.string.update_profile_success_userProfileActivity, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.image_saving_error, Toast.LENGTH_SHORT).show();
                }
            }
            else{
                NotificationUtil.showNotification(context,NotificationID.UPLOAD_FAILED_NOTIFICATION_ID);
                Toast.makeText(context,R.string.network_error,Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class UpdateServerUserProfileNameTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        String newUserName;

        public UpdateServerUserProfileNameTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... names) {
            NotificationUtil.showNotification(context, NotificationID.UPDATE_USER_PROFILE_NAME_NOTIFICATION_ID);
            newUserName = names[0];
            BasicNameValuePair[] params = new BasicNameValuePair[1];
            params[0] = new BasicNameValuePair(DrupalServicesFieldKeysConst.USER_NAME, newUserName);
            try {
                HashMap<String, String> updateResultMap = drupalServicesUser.update(params, PreferenceUtil.getCurrentUserID(context));
                if (updateResultMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals("200"))
                    return true;
                else
                    return false;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            NotificationUtil.removeNotification(context,NotificationID.UPDATE_USER_PROFILE_NAME_NOTIFICATION_ID);
            if (result) {
                updateLocalUserName(newUserName, context);
            }else {
                NotificationUtil.showNotification(context, NotificationID.UPLOAD_FAILED_NOTIFICATION_ID);
            }
        }
    }
    //AsyncTask for upload location, ReverseGeoCodeTask ---call---> UpdateServerUserLocationTask
    private class ReverseGeoCodeTask extends AsyncTask<LatLng, Void, Address> {
        Context context;
        LatLng latLng;
        int requestCode;
        public ReverseGeoCodeTask(Context context,int requestCode) {
            this.context = context;
            this.requestCode=requestCode;
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
                //Show Dialog to select choose another address or not
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.address_not_found_choose_again_msg).setTitle(R.string.address_not_found_title);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        try {
                            startActivityForResult(builder.build((Activity) (context)), PROFILE_ADDRESS1_PICKER_REQUEST);
                        } catch (Exception ex) {
                            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                updateServerUserLocationTask = new UpdateServerUserLocationTask(context, latLng, address,requestCode);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    updateServerUserLocationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    updateServerUserLocationTask.execute();
                }
            }
        }
    }
    private class UpdateServerUserLocationTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        LatLng latLng;
        Address address;
        int requestCode;
        public UpdateServerUserLocationTask(Context context, LatLng latLng, Address address,int requestCode) {
            this.context = context;
            this.latLng = latLng;
            this.address = address;
            this.requestCode=requestCode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            NotificationUtil.showNotification(context, NotificationID.UPDATE_USER_PROFILE_ADDRESS_NOTIFICATION_ID);
            boolean updateResult = updateServerUserLocation(latLng, address, requestCode);
            return updateResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            NotificationUtil.removeNotification(context,NotificationID.UPDATE_USER_PROFILE_ADDRESS_NOTIFICATION_ID);
            if (result) {
                updateLocalUserLocation(context,address,requestCode);
            }else {
                NotificationUtil.showNotification(context, NotificationID.UPLOAD_FAILED_NOTIFICATION_ID);
                Toast.makeText(context,R.string.failed_update_address,Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class LogoutUserTask extends AsyncTask<Void, Void, Void> {
        Context context;
        DrupalServicesUser drupalServicesUser;

        public LogoutUserTask(Context context, DrupalServicesUser drupalServicesUser) {
            this.context = context;
            this.drupalServicesUser = drupalServicesUser;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                drupalServicesUser.logout(context);
                MyObservationCacheManager.getInstance(context).clearCache();
                deleteLocalUserInfo(context);
            } catch (Exception ex) {}
            return null;
        }
    }
    private class DetectUserInfoChangesTask extends AsyncTask<Void,Void,Void>{
        Context context;
        public DetectUserInfoChangesTask(Context context){
            this.context=context;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String,String> map= drupalServicesUser.getUser(Integer.parseInt(PreferenceUtil.getCurrentUserID(context)));
                if (map.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)){
                    updateLocalUserProfile(map.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
                }
            }catch ( Exception ex){}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initializeView();
        }
    }
    private void updateLocalUserProfile(String jsonStr){
        try{
            JSONObject jsonObject=new JSONObject(jsonStr);
            try{
                String username=jsonObject.getString(USER_NAME);
                PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_NAME,username);
            }catch (Exception ex){}
            try {
                String email = jsonObject.getString(EMAIL);
                PreferenceUtil.saveString(this, SharedPreferencesConst.K_USER_EMAIL, email);
            }catch (Exception ex){}
            try {
                JSONObject pictureObject = jsonObject.getJSONObject(PICTURE);
                String pictureUrl=pictureObject.getString(PICTURE_URL);
                if (pictureUrl==null||pictureUrl.isEmpty()) {
                    deleteLocalUserProfileImage(this);
                }else {
                    if (!pictureUrl.equals(PreferenceUtil.getCurrentUserPictureServerUri(this))) {
                        Bitmap photo = PhotoUtil.getBitmapFromServerURL(pictureUrl);
                        updateLocalUserProfileImage(photo, this);
                        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_IMAGE_SERVER_URI,pictureUrl);
                    }
                }
            }catch (Exception ex){}
            try{
                Address address=new Address(Locale.getDefault());
                JSONObject address1Object = jsonObject.getJSONObject(ADDRESS1).getJSONArray(UND).getJSONObject(0);
                address.setCountryName(address1Object.getString(COUNTRY));
                address.setAdminArea(address1Object.getString(ADMIN_AREA));
                address.setLocality(address1Object.getString(LOCALITY));
                address.setThoroughfare(address1Object.getString(THOROUGHFARE));
                PreferenceUtil.saveString(this, SharedPreferencesConst.K_USER_ADDRESS_1, GeoUtil.getFullAddress(address));

            }catch (Exception ex){
                PreferenceUtil.deleteKey(this, SharedPreferencesConst.K_USER_ADDRESS_1);
            }
            try{
                Address address=new Address(Locale.getDefault());
                JSONObject address2Object = jsonObject.getJSONObject(ADDRESS2).getJSONArray(UND).getJSONObject(0);
                address.setCountryName(address2Object.getString(COUNTRY));
                address.setAdminArea(address2Object.getString(ADMIN_AREA));
                address.setLocality(address2Object.getString(LOCALITY));
                address.setThoroughfare(address2Object.getString(THOROUGHFARE));
                PreferenceUtil.saveString(this, SharedPreferencesConst.K_USER_ADDRESS_2, GeoUtil.getFullAddress(address));
            }catch (Exception ex){
                PreferenceUtil.deleteKey(this, SharedPreferencesConst.K_USER_ADDRESS_2);
            }

        }catch(Exception ex){}
    }

    /******** Update Server User Profile Image function group***************/
    private boolean updateServerUserProfileImage(Bitmap bitmap) throws Exception {
        int oldFid = getUserImageFileID();
        int newFid = uploadNewUserProfileImage(bitmap);
        boolean updateResult = false;
        if (newFid != -1) {
            updateResult = updateUserImageFid(newFid);
        }
        if (updateResult) {
            /*It doesn't matter if we failed to delete the old file
            *So just log without telling the user
            */
            try {
                deleteOldProfileImage(oldFid);
            } catch (Exception ex) {
                Log.i("DELETE_FILE_FAILED", "Failed to Delete file" + oldFid);
            }
            return true;
        } else {
            /*If not successfully link user with the new image
            * but uploaded the new image to server
            * Delete the uploaded file*/
            if (newFid != -1) {
                try {
                    deleteOldProfileImage(newFid);
                } catch (Exception ex) {
                    Log.i("DELETE_FILE_FAILED", "Failed to Delete file" + newFid);
                }
            }
            return false;
        }
    }
    private int getUserImageFileID() throws Exception {
        HashMap<String, String> fetchUserInfoResponse = drupalServicesUser.getUser(Integer.valueOf(PreferenceUtil.getCurrentUserID(this)));
        if (fetchUserInfoResponse.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals("200")) {
            String resposneString = fetchUserInfoResponse.get(DrupalServicesFieldKeysConst.RESPONSE_BODY);
            JSONObject resposneObject = new JSONObject(resposneString);
            JSONObject pictureObjecct;
            //Picture object may not exist,then return -1 to tell caller that user doesn't have a picture
            try {
                pictureObjecct = resposneObject.getJSONObject(DrupalServicesFieldKeysConst.USER_PICTURE);
                return pictureObjecct.getInt(FILE_FILE_ID);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
    private int uploadNewUserProfileImage(Bitmap bitmap) throws Exception {
        String filename = "user_profile_" + PreferenceUtil.getCurrentUser(this) + "_" + System.currentTimeMillis() + ".jpg";
        String fileServerPath = "public://" + filename;
        String encoded = UploadUtil.getBase64StringFromBitmap(bitmap);
        BasicNameValuePair[] params = UploadUtil.constructBasicFileUploadParams(filename, fileServerPath, encoded);
        HashMap<String, String> createFileResponse = drupalServicesFile.create(params);
        if (createFileResponse.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals("200")) {
            JSONObject responseObject = new JSONObject(createFileResponse.get(DrupalServicesFieldKeysConst.RESPONSE_BODY));
            int fid = responseObject.getInt(FILE_FILE_ID);
            return fid;
        }
        return INVALID;
    }
    private void deleteOldProfileImage(int fid) throws Exception {
        drupalServicesFile.delete(fid);
    }
    private boolean updateUserImageFid(int fid) throws Exception {
        BasicNameValuePair[] params = new BasicNameValuePair[1];
        params[0] = new BasicNameValuePair(DrupalServicesFieldKeysConst.USER_PICTURE, String.valueOf(fid));
        HashMap<String, String> responseMap = drupalServicesUser.update(params, PreferenceUtil.getCurrentUserID(this));
        return responseMap.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals("200");
    }
    /********** Upload User Location*************/
    private boolean updateServerUserLocation(LatLng latLng,Address address,int requestCode) {
        double lat = latLng.latitude;
        double lon = latLng.longitude;
        String addressField="";
        ArrayList<BasicNameValuePair> paramList = new ArrayList<>();
       /* paramList.add(new BasicNameValuePair("input_format","GEOFIELD_INPUT_LAT_LON"));
        paramList.add(new BasicNameValuePair("field_location_lat_long[und][0][geom][lat]", String.valueOf(lat)));
        paramList.add( new BasicNameValuePair("field_location_lat_long[und][0][geom][lon]", String.valueOf(lon)));*/

        switch (requestCode){
            case PROFILE_ADDRESS1_PICKER_REQUEST:addressField= DrupalServicesFieldKeysConst.LOGIN_ADDRESS_1;break;
            case PROFILE_ADDRESS2_PICKER_REQUEST:addressField= DrupalServicesFieldKeysConst.LOGIN_ADDRESS_2;break;
        }
        //TODO: Country code may not be accepted by Drupal,need a better solution in the future
        if (address==null)
            return false;
        String countryCode= address.getCountryCode();
        String locality=address.getLocality();
        String thoroughfare=address.getThoroughfare();
        String postalCode=address.getPostalCode();
        String feature=address.getFeatureName();
        if (countryCode!=null)
            paramList.add(new BasicNameValuePair(addressField+"[und][0][country]",countryCode));
        if (locality!=null)
            paramList.add(new BasicNameValuePair(addressField+"[und][0][locality]",locality));
        if (thoroughfare!=null)
            paramList.add(new BasicNameValuePair(addressField+"[und][0][thoroughfare]", String.format("%s,%s", feature, thoroughfare)));
        if (postalCode!=null)
            paramList.add(new BasicNameValuePair(addressField+"[und][0][postal_code]",postalCode));

        BasicNameValuePair[] params = paramList.toArray(new BasicNameValuePair[paramList.size()]);
        try {
            HashMap<String, String> updateResult = drupalServicesUser.update(params, PreferenceUtil.getCurrentUserID(this));
            if (!updateResult.get(DrupalServicesFieldKeysConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    /************Update Local Cached User Profile Info********/
    private boolean updateLocalUserProfileImage(Bitmap photo, Context context) {
        if (!deleteLocalUserProfileImage(context))
            return false;
        String newFilePath = new File(context.getCacheDir(), "userImage_small_" + PreferenceUtil.getCurrentUser(context) + "_" + System.currentTimeMillis()).getPath();
        try {
            FileOutputStream out = new FileOutputStream(newFilePath);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
            PreferenceUtil.saveString(context, SharedPreferencesConst.K_USER_IMAGE_LOCAL_URI, newFilePath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean updateLocalUserName(String newUserName, Context context) {
        PreferenceUtil.saveString(context, SharedPreferencesConst.K_USER_NAME, newUserName);
        ((TextView) (findViewById(R.id.txtUserName_UserProfileActivity))).setText(newUserName);
        Toast.makeText(context, R.string.update_profile_success_userProfileActivity, Toast.LENGTH_SHORT).show();

        return true;
    }
    private boolean updateLocalUserLocation(Context context,Address address,int requestCode){
        switch (requestCode){
            case PROFILE_ADDRESS1_PICKER_REQUEST:
            {
                TextView txtAddress=(TextView)findViewById(R.id.txtAddress1_UserProfileActivity);
                String fullAddress=GeoUtil.getFullAddress(address);
                txtAddress.setText(fullAddress);
                PreferenceUtil.saveString(context,SharedPreferencesConst.K_USER_ADDRESS_1,fullAddress);
                break;
            }
            case PROFILE_ADDRESS2_PICKER_REQUEST:
            {
                String fullAddress= GeoUtil.getFullAddress(address);
                TextView txtAddress=(TextView)findViewById(R.id.txtAddress2_UserProfileActivity);
                txtAddress.setText(fullAddress);
                PreferenceUtil.saveString(context,SharedPreferencesConst.K_USER_ADDRESS_2,fullAddress);
                break;
            }
        }
        Toast.makeText(context, R.string.update_profile_success_userProfileActivity, Toast.LENGTH_SHORT).show();

        return true;
    }
    private boolean deleteLocalUserSessionInfo(Context context){
        PreferenceUtil.deleteKey(context, SharedPreferencesConst.K_SESSION_EXPIRED);
        return true;
    }
    private boolean deleteLocalUserProfileImage(Context context){
        try {
            String currentProfilePhotoPath = PreferenceUtil.getCurrentUserPictureLocalUri(context);
            File currentProfilePhoto = new File(currentProfilePhotoPath);
            if (currentProfilePhoto.exists())
                return currentProfilePhoto.delete();
        }catch (Exception ex){
            return false;
        }
        return true;
    }
    private boolean deleteLocalUserInfo(Context context){
        deleteLocalUserProfileImage(context);
        PreferenceUtil.deleteKey(context, SharedPreferencesConst.K_USER_ID);
        PreferenceUtil.deleteKey(context,SharedPreferencesConst.K_USER_NAME);
        PreferenceUtil.deleteKey(context,SharedPreferencesConst.K_USER_IMAGE_SERVER_URI);
        PreferenceUtil.deleteKey(context,SharedPreferencesConst.K_USER_EMAIL);
        PreferenceUtil.deleteKey(context,SharedPreferencesConst.K_USER_ADDRESS_1);
        PreferenceUtil.deleteKey(context,SharedPreferencesConst.K_USER_ADDRESS_2);

        return true;
    }

}

