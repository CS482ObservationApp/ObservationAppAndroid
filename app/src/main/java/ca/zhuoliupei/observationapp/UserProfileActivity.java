package ca.zhuoliupei.observationapp;

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

public class UserProfileActivity extends AppCompatActivity {
    String baseUrl, endpoint;
    DrupalAuthSession authSession;
    DrupalServicesUser drupalServicesUser;
    DrupalServicesFile drupalServicesFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        initializeVaribles();
        initializeView();
        setViewOnClickListeners();
    }

    private void initializeVaribles() {
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

    private void setViewOnClickListeners() {
        //TODO:
        setProfilePhotoOnClickListener();
        setUserNameOnClickListener();
    }

    private void setProfilePhotoOnClickListener() {
         findViewById(R.id.ll_photo_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivityForResult(new Intent((v.getContext()), ChooseUploadPhotoMethodActivity.class), RequestIDConst.CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST);
             }
         });
    }
    private void setUserNameOnClickListener(){
        findViewById(R.id.ll_name_UserProfileActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent((v.getContext()), ChangeUserNameActivity.class), RequestIDConst.CHANGE_USER_NAME_REQUEST);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestIDConst.CHOOSE_UPLOAD_PHOTO_METHOD_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        return;
                    }
                    int result = data.getIntExtra("result", RequestIDConst.CANCEL_UPLOAD);
                    startPickingPhotoActivity(result);
                }
                break;
            }
            case RequestIDConst.GET_PHOTO_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data == null)
                        return;
                    Bitmap photo;
                    //If photo comes from camera,use this to get bitmap
                    photo=(Bitmap)data.getExtras().get("data");
                    //If photo comes from gallary,photo would be null,use the helper method
                    if (photo==null)
                        photo=getBitmapFromUri(data.getData());

                    new UpdateServerUserProfileImageTask(this).execute(photo);
                }
                break;
            }
        }
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
    private void startPickingPhotoActivity(int result) {
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
                boolean updateResult = updateServerUserProfileImage(photo);
                return updateResult;
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

    /********  Update Server User Profile Image function group***************/
    private boolean updateServerUserProfileImage(Bitmap bitmap) throws Exception{
        int oldFid=getUserImageFileID();
        int newFid=uploadNewUserProfileImage(bitmap);
        boolean result=updateUserImage(newFid);
        if (result){
            /*It doesn't matter if we failed to delete the old file
            *So just log without telling the user
            */
            try {
                deleteOldUserProfileImage(oldFid);
            }catch (Exception ex) {
                Log.i("DELETE_FILE_FAILED","Failed to Delete file"+oldFid);
            }
            return true;
        }
        return false;
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
    private void deleteOldUserProfileImage(int fid) throws Exception{
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
}
