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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import Const.SharedPreferencesConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.DownLoadUtil;
import HelperClass.PreferenceUtil;
import HelperClass.RegexValidator;
import HelperClass.ToolBarStyler;
/**
 * This Activity class has the following functions:
 * 1. provide a UI for getting user name and password to login
 * 2. provide entry to Register Activity and Newest Observation Activity
 * 3. login and store user profile info to local
 */

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEditText,passwordEditText;
    private String userNameStr,passwordStr;
    private String baseUrl,endpoint;
    private TextView txtLink;
    private LoginTask loginTask;
    private DownloadUserImageTask downloadUserImageTask;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginTask!=null)
            loginTask.cancel(true);
        //No need to cancel downloadUserImageTask
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeVariables();
        initializeUI();
        setWidgetListeners();
    }


    //Initialization Wrappers
    private void initializeVariables(){
        baseUrl=getResources().getString(R.string.drupal_site_url);
        endpoint=getResources().getString(R.string.drupal_server_endpoint);

        txtLink=((TextView)findViewById(R.id.reset_link_LoginActivity));
        if (txtLink!=null)
            txtLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void initializeUI(){
        initializeActionBar();
    }
    private void setWidgetListeners(){
        setRegisterBtnOnClick();
        setLoginBtnOnClick();
        setLoginLaterOnClick();
    }

    //Wrapped in initializeUI()
    private void initializeActionBar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_LoginActivity);
        ToolBarStyler.styleToolBar(this, myToolbar, "Login");
    }

    //Wrapped in setWidgetListeners()
    private void setRegisterBtnOnClick(){
        //When register button is clicked, start a register activity
        findViewById(R.id.btnRegister__LoginActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private void setLoginBtnOnClick(){

        //When login button is clicked, validate input and log user in
        findViewById(R.id.btnLogin_LoginActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginTask = new LoginTask(v.getContext());
                //Get input
                userNameEditText = ((EditText) findViewById(R.id.txtUserName_LoginActivity));
                passwordEditText = ((EditText) findViewById(R.id.txtPassword_LoginActivity));
                userNameStr = userNameEditText.getText().toString().trim();
                passwordStr = passwordEditText.getText().toString().trim();

                //Validate input
                if (validateInputOnClient()) {
                    loginTask.execute();
                    showTransitionView();
                }
            }
        });
    }
    private void setLoginLaterOnClick(){
        findViewById(R.id.txtLoginLater_LoginActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,NewestObservationsActivity.class);
                startActivity(intent);
            }
        });
    }

    // Asyntasks
    private class LoginTask extends AsyncTask<Void,Void,HashMap<String,String>>{
        Context context;

        public LoginTask(Context context){
            this.context=context;
        }
        public HashMap<String,String> doInBackground(Void... voids){
            DrupalAuthSession authSession=new DrupalAuthSession();
            DrupalServicesUser drupalServicesUser =new DrupalServicesUser(baseUrl,endpoint);
            drupalServicesUser.setAuth(authSession);

            try {
                return drupalServicesUser.login(userNameStr, passwordStr);
            }catch (Exception e){
                return new HashMap<String,String>();
            }
        }

        @Override
        protected void onPostExecute(HashMap<String ,String> responseMap) {
            hideTransitionView();

            String statusCode=responseMap.get(DrupalServicesFieldKeysConst.STATUS_CODE);
            String responseBody=responseMap.get(DrupalServicesFieldKeysConst.RESPONSE_BODY);

            if (statusCode==null||statusCode.isEmpty()||responseBody==null||responseBody.isEmpty())
            {
                Toast.makeText(context,R.string.network_error,Toast.LENGTH_LONG).show();
                return;
            }

            /*If login successfully, store the return seesion info as well as user info into
              Shared Preferences for further usage, and direct to Newest Observations Activity
            */

            if (statusCode.equals(HTTPConst.HTTP_OK_200)) {
                if (!storeSessionInfoToSharedPreferences(responseBody))
                    return;
                if (!storeUserInfoToSharedPreferences(responseBody))
                    return;
                downloadUserImageTask=new DownloadUserImageTask(context);
                downloadUserImageTask.execute(PreferenceUtil.getCurrentUserPictureServerUri(context));
                startActivity(new Intent(LoginActivity.this, NewestObservationsActivity.class));
            }

            /*
            If login is not successful, show the error message.
            If it's because of wrong password credential, prompt user to reset password
            */
                else if (statusCode.equals(HTTPConst.HTTP_UNAUTHORIZED_401) || statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)) {
                    if (statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)) {
                        txtLink.setText(getText(R.string.reset_password_link_loginActivity));
                    }
                    ((TextView) findViewById(R.id.txtMessage_LoginActivity)).setText(responseBody);
                }

            /*If server response other errors, just show network error message*/
                else {
                    Toast.makeText(context,getText(R.string.network_error).toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    private class DownloadUserImageTask extends AsyncTask<String,Void,Void>{
        Context context;
        public DownloadUserImageTask(Context context){
            this.context=context;
        }
        @Override
        protected Void doInBackground(String... params) {
            String imgServerUrl=params[0];
            if (imgServerUrl!=null) {
                String userName = PreferenceUtil.getCurrentUser(context);
                String pathToSave = new File(context.getFilesDir(), "userImage_small_" + userName + "_" + System.currentTimeMillis()).getPath();
                boolean downloadSucceeded = DownLoadUtil.downloadImage(imgServerUrl, pathToSave);
                if (downloadSucceeded) {
                    PreferenceUtil.saveString(context, SharedPreferencesConst.K_USER_IMAGE_LOCAL_URI, pathToSave);
                }
            }
            return null;
        }
    }

    //Helper Methods
    private boolean validateInputOnClient(){
        boolean validate=true;

        if (userNameStr.isEmpty()){
            userNameEditText.setError(getResources().getString(R.string.field_empty_error));
            validate=false;
        }else if (!RegexValidator.validate(userNameStr, RegexValidator.InputType.USERNAME))
        {
            userNameEditText.setError(getResources().getString(R.string.username_contains_illeagle_char));
            validate=false;
        }

        if (passwordStr.isEmpty()) {
            passwordEditText.setError(getResources().getString(R.string.field_empty_error));
            validate = false;
        }

        return validate;
    }
    private boolean storeSessionInfoToSharedPreferences(String responseBody) {
        JSONObject responseJsonObject;
        String sessionID,sessionName,cookie;
        try {
            //Fetch session info and store it
            responseJsonObject = new JSONObject(responseBody);
            sessionID = responseJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_SESSION_ID);
            sessionName = responseJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_SESSION_NAME);
            cookie=sessionName+"="+ sessionID;
        }catch (Exception e) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            return  false;
        }
        PreferenceUtil.saveString(this, SharedPreferencesConst.K_SESSION_ID, sessionID);
        PreferenceUtil.saveString(this, SharedPreferencesConst.K_SESSION_NAME, sessionName);
        PreferenceUtil.saveString(this, SharedPreferencesConst.K_COOKIE, cookie);
        PreferenceUtil.saveBoolean(this, SharedPreferencesConst.K_SESSION_EXPIRED, false);
        return true;
    }
    private boolean storeUserInfoToSharedPreferences(String responseBody){
        JSONObject responseJsonObject,userJsonObject,userPictureJsonObject,address1JsonObject,address2JsonObject;
        String uid,userName,email,address1,address2,pictureServerUrl;
        try {
            responseJsonObject = new JSONObject(responseBody);
            userJsonObject = responseJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_USER);

            uid = userJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_USER_ID);
            userName = userJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_NAME);
        }catch (Exception e) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            return false;
        }

        /*The following item may not be included in response JSON
        * If doesn't exist,just leave it empty
        */
        try {
            userPictureJsonObject = userJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_PICTURE);
            pictureServerUrl=getPictureServerUrlFromJsonObject(userPictureJsonObject);
        }catch (Exception e){
            pictureServerUrl="";
        }
        try {
            address1JsonObject = userJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_ADDRESS_1);
            address1=getAddressFromAddressJsonObject(address1JsonObject);
        }catch (Exception e){
            address1="";
        }
        try {
            address2JsonObject = userJsonObject.getJSONObject(DrupalServicesFieldKeysConst.LOGIN_ADDRESS_2);
            address2=getAddressFromAddressJsonObject(address2JsonObject);
        }catch (Exception e){
            address2="";
        }

        email=getEmailFromUserJsonObject(userJsonObject);


        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_ID,uid);
        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_NAME,userName);
        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_IMAGE_SERVER_URI,pictureServerUrl);
        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_EMAIL,email);
        PreferenceUtil.saveString(this,SharedPreferencesConst.K_USER_ADDRESS_1,address1);
        PreferenceUtil.saveString(this, SharedPreferencesConst.K_USER_ADDRESS_2, address2);

        return true;
    }
    private String getPictureServerUrlFromJsonObject(JSONObject object) {
        try {
            String url = object.getString(DrupalServicesFieldKeysConst.LOGIN_URL);
            if (url == null)
                url = "";
            return url;
        }catch (Exception e) {
            return "";
        }
    }
    private String getAddressFromAddressJsonObject(JSONObject object){
        try {
            JSONArray jsonArray = object.getJSONArray(DrupalServicesFieldKeysConst.LOGIN_UND);
            JSONObject addressJsonObject = jsonArray.getJSONObject(0);
            String country = addressJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_COUNTRY);
            String adminArea = addressJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_ADMIN_AREA);
            String locality = addressJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_LOCALITY);
            String thoroughfare = addressJsonObject.getString(DrupalServicesFieldKeysConst.LOGIN_THOROUGHFARE);

            return thoroughfare+"\r\n"+locality+","+adminArea+"\r\n"+country;
        }catch (Exception e){
            return "";
        }
    }
    private String getEmailFromUserJsonObject(JSONObject object) {
        try {
            String email = object.getString(DrupalServicesFieldKeysConst.LOGIN_EMAIL);
            if (email==null)
                email="";
            return email;
        }catch (Exception e) {
            return "";
        }

    }
    private void hideTransitionView(){
        findViewById(R.id.txtPassword_LoginActivity).setEnabled(true);
        findViewById(R.id.txtUserName_LoginActivity).setEnabled(true);
        findViewById(R.id.txtLoginLater_LoginActivity).setEnabled(true);
        findViewById(R.id.btnLogin_LoginActivity).setEnabled(true);
        findViewById(R.id.btnRegister__LoginActivity).setEnabled(true);

        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_LoginActivity);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setVisibility(View.GONE);
    }
    private void showTransitionView(){
        findViewById(R.id.txtPassword_LoginActivity).setEnabled(false);
        findViewById(R.id.txtUserName_LoginActivity).setEnabled(false);
        findViewById(R.id.txtLoginLater_LoginActivity).setEnabled(false);
        findViewById(R.id.btnLogin_LoginActivity).setEnabled(false);
        findViewById(R.id.btnRegister__LoginActivity).setEnabled(false);

        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_LoginActivity);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

}
