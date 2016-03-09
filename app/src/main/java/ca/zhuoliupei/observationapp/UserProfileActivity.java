package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlacePicker;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import Const.DrupalServicesResponseConst;
import Const.RequestIDConst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import Const.SharedPreferencesConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesFile;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import HelperClass.UploadUtil;

public class UserProfileActivity extends AppCompatActivity{
    String baseUrl, endpoint;
    DrupalAuthSession authSession;
    DrupalServicesUser drupalServicesUser;
    DrupalServicesFile drupalServicesFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        initializeVariables();
        initializeView();
        setViewOnClickListeners();
    }

    private void initializeVariables() {
        baseUrl = getText(R.string.drupal_site_url).toString();
        endpoint = getText(R.string.drupal_server_endpoint).toString();
        authSession = new DrupalAuthSession();
        authSession.setSession(PreferenceUtil.getCookie(this));
        drupalServicesUser = new DrupalServicesUser(baseUrl, endpoint);
        drupalServicesFile=new DrupalServicesFile(baseUrl,endpoint);
        drupalServicesUser.setAuth(authSession);
        drupalServicesFile.setAuth(authSession);
    }
    private void initializeView() {
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

        File imgFile = new File(imgFilePath);
        if (imgFile.exists())
            profileImageIV.setImageURI(Uri.parse(imgFilePath));
        else profileImageIV.setImageResource(R.drawable.icon_user_default);

        userNameTV.setText(userName);
        emailTV.setText(email);
        address1TV.setText(address1);
        address2TV.setText(address2);
    }

    /*
    * setViewOnClickListeners contains the other setOnClickListener Methods
    */
    private void setViewOnClickListeners() {
        //TODO:
        setProfilePhotoOnClickListener();
        setUserNameOnClickListener();
        setAddress1OnClickListener();
    }
    private void setUserNameOnClickListener(){
        findViewById(R.id.ll_name_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChangeTextFieldActivity.class), RequestIDConst.CHANGE_USER_NAME_REQUEST);
            }
        });
    }
    private void setAddress1OnClickListener(){
        findViewById(R.id.ll_address1_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build((Activity) (v.getContext())), RequestIDConst.PROFILE_ADDRESS1_PICKER_REQUEST);
                }catch (Exception ex){
                    Toast.makeText(v.getContext(),R.string.network_error,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setProfilePhotoOnClickListener() {
        findViewById(R.id.ll_photo_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChooseUploadPhotoMethodActivity.class), RequestIDConst.CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST);
            }
        });
    }

    private void startPickingPhoto(int result) {
        switch (result) {
            case RequestIDConst.CHOOSE_PHOTO_FROM_CAMERA_REQUEST: {
                PhotoUtil.launchCameraForPhoto(this);
                break;
            }
            case RequestIDConst.CHOOSE_PHOTO_FROM_GALARY_REQUEST: {
                PhotoUtil.launchGalleryAppForPhoto(this);
                break;
            }
        }
    }

    /* Handle Activity Results*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestIDConst.CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST: {
                handleChooseUploadMethodRequestResult(resultCode, data);
                break;
            }
            case RequestIDConst.GET_PHOTO_REQUEST: {
                handleGetPhotoRequestResult(resultCode, data);
                break;
            }
            case RequestIDConst.CHANGE_USER_NAME_REQUEST:{
                handleChangeUserNameRequestResult(resultCode,data);
                break;
            }
            case RequestIDConst.PROFILE_ADDRESS1_PICKER_REQUEST:{

                break;
            }
        }
    }
    private void handleChooseUploadMethodRequestResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            int result = data.getIntExtra("result", RequestIDConst.CANCEL_UPLOAD);
            startPickingPhoto(result);
        }
    }
    private void handleGetPhotoRequestResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            if (data == null)
                return;
            Bitmap photo;
            //If photo comes from camera,use this to get bitmap
            Bundle bundle=data.getExtras();
            if(bundle!=null)
                photo=(Bitmap)bundle.get("data");
            //If photo comes from gallary,photo would be null,use the helper method
            else
                photo=getBitmapFromUri(data.getData());

            new UpdateServerUserProfileImageTask(this).execute(photo);
        }
    }
    private void handleChangeUserNameRequestResult(int resultCode,Intent data){
        if (resultCode == RESULT_OK) {
            if (data == null)
                return;
            String newName=data.getExtras().getString("result");
            if (newName!=null&&!newName.isEmpty())
                new UpdateServerUserProfileNameTask(this).execute(newName);
        }
    }
    private void handleChangeAddress1RequestResult(){}
    private class UpdateServerUserProfileImageTask extends AsyncTask<Bitmap,Void,Boolean>{
        Bitmap photo;
        Context context;
        public UpdateServerUserProfileImageTask(Context context){
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(Bitmap... params) {
            photo=params[0];
            try {
                return updateServerUserProfileImage(photo);
            }catch (Exception ex){
                 return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean updateResult) {
            if (updateResult){
                boolean saveResult=updateLocalUserProfileImage(photo,context);
                if (saveResult){
                    ((ImageView)findViewById(R.id.imgUserImage_UserProfileActivity)).setImageBitmap(photo);
                }else {
                    Toast.makeText(context, R.string.image_saving_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private class UpdateServerUserProfileNameTask extends AsyncTask<String,Void,Boolean>{
        Context context;
        String newUserName;
        public UpdateServerUserProfileNameTask(Context context){
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(String... names) {
            newUserName=names[0];
            BasicNameValuePair[] params=new BasicNameValuePair[1];
            params[0]=new BasicNameValuePair(DrupalServicesResponseConst.USER_NAME,newUserName);
            try {
                HashMap<String,String> updateResultMap = drupalServicesUser.update(params, PreferenceUtil.getCurrentUserID(context));
                if (updateResultMap.get(DrupalServicesResponseConst.STATUS_CODE).equals("200"))
                    return true;
                else
                    return false;
            }catch (Exception ex){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                PreferenceUtil.saveString(context,SharedPreferencesConst.K_USER_NAME,newUserName);
                ((TextView)(findViewById(R.id.txtUserName_UserProfileActivity))).setText(newUserName);
            }
        }
    }

    /********  Update Server User Profile Image function group***************/
    private boolean updateServerUserProfileImage(Bitmap bitmap) throws Exception{
        int oldFid=getUserImageFileID();
        int newFid=uploadNewUserProfileImage(bitmap);
        boolean updateResult=false;
        if (newFid!=-1) {
            updateResult = updateUserImage(newFid);
        }
        if (updateResult){
            /*It doesn't matter if we failed to delete the old file
            *So just log without telling the user
            */
            try {
                deleteProfileImage(oldFid);
            }catch (Exception ex) {
                Log.i("DELETE_FILE_FAILED","Failed to Delete file"+oldFid);
            }
            return true;
        }else {
            /*If not successfully link user with the new image
            * but uploaded the new image to server
            * Delete the uploaded file*/
            if (newFid!=-1) {
                try {
                    deleteProfileImage(newFid);
                }catch (Exception ex){
                    Log.i("DELETE_FILE_FAILED","Failed to Delete file"+newFid);
                }
            }
            return false;
        }
    }
    private int getUserImageFileID() throws Exception{
        HashMap<String, String> fetchUserInfoResponse = drupalServicesUser.getUser(Integer.valueOf(PreferenceUtil.getCurrentUserID(this)));
        if (fetchUserInfoResponse.get(DrupalServicesResponseConst.STATUS_CODE).equals("200")) {
            String resposneString=fetchUserInfoResponse.get(DrupalServicesResponseConst.LOGIN_RESPONSE_BODY);
            JSONObject resposneObject=new JSONObject(resposneString);
            JSONObject pictureObjecct;
            //Picture object may not exist,then return -1 to tell caller that user doesn't have a picture
            try {
                pictureObjecct = resposneObject.getJSONObject(DrupalServicesResponseConst.USER_PICTURE);
                return pictureObjecct.getInt(DrupalServicesResponseConst.FILE_FILE_ID);
            }catch (Exception e){
                return -1;
            }
        }
        return -1;
    }
    private void deleteProfileImage(int fid) throws Exception{
        drupalServicesFile.delete(fid);
    }
    private int uploadNewUserProfileImage(Bitmap bitmap) throws Exception{
        String filename = "user_profile_" + PreferenceUtil.getCurrentUser(this) + "_" + System.currentTimeMillis() + ".jpg";
        String fileServerPath = "public://" + filename;
        String encoded = UploadUtil.getBase64StringFromBitmap(bitmap);
        BasicNameValuePair[] params = UploadUtil.constructBasicFileUploadParams(filename, fileServerPath, encoded);
        HashMap<String,String> createFileResponse = drupalServicesFile.create(params);
        if (createFileResponse.get(DrupalServicesResponseConst.STATUS_CODE).equals("200")){
            JSONObject responseObject=new JSONObject(createFileResponse.get(DrupalServicesResponseConst.LOGIN_RESPONSE_BODY));
            int fid=responseObject.getInt(DrupalServicesResponseConst.FILE_FILE_ID);
            return fid;
        }
        return -1;
    }
    private boolean updateUserImage(int fid) throws Exception{
        BasicNameValuePair[] params=new BasicNameValuePair[1];
        params[0]=new BasicNameValuePair(DrupalServicesResponseConst.USER_PICTURE,String.valueOf(fid));
        HashMap<String ,String> responseMap = drupalServicesUser.update(params, PreferenceUtil.getCurrentUserID(this));
        return responseMap.get(DrupalServicesResponseConst.STATUS_CODE).equals("200");
    }
    private Bitmap getBitmapFromUri(Uri uri){
        try {
            // Let's read picked image path using content resolver
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        }catch (Exception ex){
            return null;
        }
    }
    /************ Update Local Cached User Profile Image********/
    private boolean updateLocalUserProfileImage(Bitmap photo,Context context){
        String currentProfilePhotoPath=PreferenceUtil.getCurrentUserPictureLocalUri(context);
        File currentProfilePhoto=new File(currentProfilePhotoPath);
        if (currentProfilePhoto.exists())
            currentProfilePhoto.delete();
        String newFilePath=new File(context.getFilesDir(), "userImage_small_" + PreferenceUtil.getCurrentUser(context) + "_" + System.currentTimeMillis()).getPath();
        try {
            FileOutputStream out = new FileOutputStream(newFilePath);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
            PreferenceUtil.saveString(context, SharedPreferencesConst.K_USER_IMAGE_LOCAL_URI, newFilePath);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
