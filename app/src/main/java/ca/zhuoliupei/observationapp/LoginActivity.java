package ca.zhuoliupei.observationapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import Const.DrupalServicesResponseConst;
import Const.HTTPConst;
import Const.SharedPreferencesConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.DownLoadUtil;
import HelperClass.PreferenceUtil;
import HelperClass.RegexValidator;
import HelperClass.ToolBarStyler;

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
    }

    //Initialize UI Methods
    private void initializeActionBar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_LoginActivity);
        ToolBarStyler.styleToolBar(this, myToolbar, "Login");
    }


    //Widgets Listeners, wrapped in setWidgetListeners()
    private void setRegisterBtnOnClick(){
        //When register button is clicked, start a register activity
        ImageButton registerBtn=(ImageButton)findViewById(R.id.imgBtnRegister__LoginActivity);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private void setLoginBtnOnClick(){

        //When login button is clicked, validate input and log user in
        ImageButton loginBtn=(ImageButton)findViewById(R.id.imgBtnLogin_LoginActivity);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginTask=new LoginTask(v.getContext());
                //Get input
                userNameEditText=((EditText)findViewById(R.id.txtUserName_LoginActivity));
                passwordEditText=((EditText)findViewById(R.id.txtPassword_LoginActivity));
                userNameStr=userNameEditText.getText().toString().trim();
                passwordStr=passwordEditText.getText().toString().trim();

                //Validate input
                if (validateLoginInfo()){
                    loginTask.execute();
                }
            }
        });
    }

    //Worker Tasks Classes
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
            String statusCode=responseMap.get(DrupalServicesResponseConst.STATUS_CODE);
            String responseBody=responseMap.get(DrupalServicesResponseConst.RESPONSE_BODY);

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
    private boolean validateLoginInfo(){
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
        JSONObject responseJsonObjcet;
        String sessionID,sessionName,cookie;
        try {
            //Fetch session info and store it
            responseJsonObjcet = new JSONObject(responseBody);
            sessionID = responseJsonObjcet.getString(DrupalServicesResponseConst.LOGIN_SESSION_ID);
            sessionName = responseJsonObjcet.getString(DrupalServicesResponseConst.LOGIN_SESSION_NAME);
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
            userJsonObject = responseJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_USER);

            uid = userJsonObject.getString(DrupalServicesResponseConst.LOGIN_USER_ID);
            userName = userJsonObject.getString(DrupalServicesResponseConst.LOGIN_NAME);
        }catch (Exception e) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
            return false;
        }

        /*The following item may not be included in response JSON
        * If doesn't exist,just leave it empty
        */
        try {
            userPictureJsonObject = userJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_PICTURE);
            pictureServerUrl=getPictureServerUrlFromJsonObject(userPictureJsonObject);
        }catch (Exception e){
            pictureServerUrl="";
        }
        try {
            address1JsonObject = userJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_ADDRESS_1);
            address1=getAddressFromAddressJsonObject(address1JsonObject);
        }catch (Exception e){
            address1="";
        }
        try {
            address2JsonObject = userJsonObject.getJSONObject(DrupalServicesResponseConst.LOGIN_ADDRESS_2);
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
            String url = object.getString(DrupalServicesResponseConst.LOGIN_URL);
            if (url == null)
                url = "";
            return url;
        }catch (Exception e) {
            return "";
        }
    }
    private String getAddressFromAddressJsonObject(JSONObject object){
        try {
            JSONArray jsonArray = object.getJSONArray(DrupalServicesResponseConst.LOGIN_UND);
            JSONObject addressJsonObject = jsonArray.getJSONObject(0);
            String country = addressJsonObject.getString(DrupalServicesResponseConst.LOGIN_COUNTRY);
            String adminArea = addressJsonObject.getString(DrupalServicesResponseConst.LOGIN_ADMIN_AREA);
            String locality = addressJsonObject.getString(DrupalServicesResponseConst.LOGIN_LOCALITY);
            String thoroughfare = addressJsonObject.getString(DrupalServicesResponseConst.LOGIN_THOROUGHFARE);

            return thoroughfare+"\r\n"+locality+","+adminArea+"\r\n"+country;
        }catch (Exception e){
            return "";
        }
    }
    private String getEmailFromUserJsonObject(JSONObject object) {
        try {
            String email = object.getString(DrupalServicesResponseConst.LOGIN_EMAIL);
            if (email==null)
                email="";
            return email;
        }catch (Exception e) {
            return "";
        }

    }

}
