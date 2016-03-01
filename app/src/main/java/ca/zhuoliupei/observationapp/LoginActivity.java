package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import Const.AppConst;
import Const.DrupalServicesResponseConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.RegexValidator;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEditText,passwordEditText;
    private String userNameStr,passwordStr;
    private String baseUrl,endpoint;
    private  TextView txtLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        baseUrl=getResources().getString(R.string.drupal_site_url);
        endpoint=getResources().getString(R.string.drupal_server_endpoint);

        txtLink=((TextView)findViewById(R.id.reset_link_LoginActivity));
        txtLink.setMovementMethod(LinkMovementMethod.getInstance());

        //When register button is clicked, start a register activity
        ImageButton registerBtn=(ImageButton)findViewById(R.id.imgBtnRegister__LoginActivity);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //When login button is clicked, validate input and log user in
        ImageButton loginBtn=(ImageButton)findViewById(R.id.imgBtnLogin_LoginActivity);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get input
                userNameEditText=((EditText)findViewById(R.id.txtUserName_LoginActivity));
                passwordEditText=((EditText)findViewById(R.id.txtPassword_LoginActivity));
                userNameStr=userNameEditText.getText().toString().trim();
                passwordStr=passwordEditText.getText().toString().trim();

                //Validate input
                if (validateLoginInfo()){
                    loginUser();
                }
            }
        });
    }

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

    private class LoginTask extends AsyncTask<Void,Void,HashMap<String,String>>{
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
            handleRequestResponse(responseMap);
        }
    }


    private boolean loginUser(){
        LoginTask loginTask=new LoginTask();
        loginTask.execute();
        return  true;
    }

    private void handleRequestResponse(HashMap<String ,String> responseMap){
        String statusCode=responseMap.get(DrupalServicesResponseConst.STATUSCODE);
        String responseBody=responseMap.get(DrupalServicesResponseConst.RESPONSEBODY);

        try {
            if (statusCode.equals(HTTPConst.HTTP_OK_200)) {

                //Fetch session info and store it
                JSONObject responseJsonObjcet=new JSONObject(responseBody);
                JSONObject userJsonObject=responseJsonObjcet.getJSONObject(DrupalServicesResponseConst.USER);
                String sessionID=responseJsonObjcet.getString(DrupalServicesResponseConst.SESSIONID);
                String sessionName=responseJsonObjcet.getString(DrupalServicesResponseConst.SESSIONNAME);
                String cookie=sessionName+"="+ sessionID;
                String userName=userJsonObject.getString(DrupalServicesResponseConst.NAME);

                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DrupalServicesResponseConst.SESSIONID, sessionID).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DrupalServicesResponseConst.SESSIONNAME, sessionName).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DrupalServicesResponseConst.COOKIE, cookie).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DrupalServicesResponseConst.USER,userName).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(AppConst.SESSION_EXPIRED, false).commit();

                startActivity(new Intent(LoginActivity.this, NewestObservationsActivity.class));
            } else if (statusCode.equals(HTTPConst.HTTP_UNAUTHORIZED_401) || statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)) {
                if (statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)) {
                    txtLink.setText(getText(R.string.reset_password_link_loginActivity));
                }
                ((TextView) findViewById(R.id.txtMessage_LoginActivity)).setText(responseBody);
            } else {
                throw new Exception();
            }
        }catch (Exception e){
            //if there's no response(statuscode and responsebody is null) show Toast
            Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }
    }


}
