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

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;

import Const.DrupalServicesFieldKeysConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.RegexValidator;
/**This class provides functions:
 * 1. Send register request to cloud and return if it's success or not*/
public class RegisterActivity extends AppCompatActivity {

    EditText userNameEditText,emailEditText;
    String userNameStr,emailStr;

    private enum ClientSideValidationResult{
        VALIDATION_PASS,
        USERNAME_ILLEGLE_CHAR_ERR,  USERNAME_EMPTY,
        EMAIL_FORMAT_ERR,           EMAIL_EMPTY,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        addLinkToText();
        setCheckBoxOnCheck();
        setRegisterBtnOnClick();
    }
    private void addLinkToText(){
        //Add link into EditText
        TextView useTermTextView=(TextView)findViewById(R.id.txtAgreeTerm_RegisterActivity);
        if (useTermTextView != null) {
            useTermTextView.setMovementMethod(LinkMovementMethod.getInstance()); //"http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-EditText-clickable"
        }
    }
    private void setCheckBoxOnCheck(){

        //CheckBox onchecked
        CheckBox agreeTermChkbox = (CheckBox) findViewById(R.id.chkbxAgreeTerm_RegisterActivity);
        agreeTermChkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button registerBtn = (Button) findViewById(R.id.btnRegister_RegisterActivity);
                if (isChecked) {
                    registerBtn.setEnabled(true);
                } else {
                    registerBtn.setEnabled(false);
                }
            }
        });
    }
    private void setRegisterBtnOnClick(){
        //Register Button onclick
        findViewById(R.id.btnRegister_RegisterActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNameEditText = (EditText) findViewById(R.id.txtUserName_RegisterActivity);
                emailEditText = (EditText) findViewById(R.id.txtEmail_RegisterActivity);
                userNameStr = userNameEditText.getText().toString().trim();
                emailStr = emailEditText.getText().toString().trim();

                validateAndRegister();
            }
        });
    }


    private void validateAndRegister(){
        ArrayList<ClientSideValidationResult> clientSideValidationResults= validateFormOnClient();

        //Validate input in client side, if passed,send it to server and get validation info back
        if (clientSideValidationResults.get(0)==ClientSideValidationResult.VALIDATION_PASS){
            postRegisterInfo();
        }else {
            showClientValidationError(clientSideValidationResults.toArray(new ClientSideValidationResult[clientSideValidationResults.size()]));
        }
    }
    private ArrayList<ClientSideValidationResult> validateFormOnClient(){
        ArrayList<ClientSideValidationResult> results=new ArrayList<>();

        if (userNameStr.isEmpty()) {
            results.add(ClientSideValidationResult.USERNAME_EMPTY);
        }
        else {
            if (!RegexValidator.validate(userNameStr, RegexValidator.InputType.USERNAME))
                results.add(ClientSideValidationResult.USERNAME_ILLEGLE_CHAR_ERR);
        }

        if (emailStr.isEmpty()) {
            results.add(ClientSideValidationResult.EMAIL_EMPTY);
        }
        else {
            if (!RegexValidator.validate(emailStr, RegexValidator.InputType.EMAIL))
                results.add(ClientSideValidationResult.EMAIL_FORMAT_ERR);
        }


        if (results.size() == 0) {
            results.add(ClientSideValidationResult.VALIDATION_PASS);
        }
        return  results;
    }

    private class RegisterAccountTask extends AsyncTask<String,Void,HashMap<String ,String>>{
        protected HashMap<String ,String> doInBackground(String... strings){
            String userNameStr=strings[0];
            String emailStr=strings[1];

            String baseUrl=getResources().getString(R.string.drupal_site_url);
            String endpoint=getResources().getString(R.string.drupal_server_endpoint);

            DrupalServicesUser drupalServicesUser=new DrupalServicesUser(baseUrl,endpoint);
            drupalServicesUser.setAuth(new DrupalAuthSession());

            try {
                return drupalServicesUser.register(userNameStr, emailStr);
            }catch (Exception e){
                return new HashMap< >();
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, String> responseMap) {
            handleRequestResponse(responseMap);
        }
    }
    private void postRegisterInfo(){
        RegisterAccountTask registerAccountTask=new RegisterAccountTask();
        registerAccountTask.execute(userNameStr,emailStr);
    }

    private void handleRequestResponse(HashMap<String,String > response){
        String statusCode =response.get(DrupalServicesFieldKeysConst.STATUS_CODE);
        String responseStr=response.get(DrupalServicesFieldKeysConst.RESPONSE_BODY);
        try{
            if(statusCode.equals(HTTPConst.HTTP_OK_200)){
                TextView msgTextView = (TextView)findViewById(R.id.txtMessage_RegisterActivity);
                msgTextView.setText(getResources().getString(R.string.register_successful));
            }else if (statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)){
                showServerValidationError(responseStr);
            }else{
                throw new Exception("unknown response");
            }
        }catch (Exception e){
            //if there's no response(statuscode and responsebody is null ) or response is not correct(Json parsing error) show Toast
            Toast.makeText(this,getResources().getString(R.string.network_error),Toast.LENGTH_LONG).show();
        }

    }

    private void showServerValidationError(String responseStr) throws JSONException {
        JSONObject responseJsonObject = new JSONObject(responseStr);
        JSONObject errorJsonObject = responseJsonObject.getJSONObject("form_errors");
        String userNameError=null;
        String emailError=null;
        try {
            userNameError = errorJsonObject.getString("name");
        }catch (Exception ex){}
        try {
            emailError = errorJsonObject.getString("mail");
        }catch (Exception ex){}
        if (emailError!=null) {
            if (!emailError.isEmpty()) {
                EditText userNameTxt=(EditText)findViewById(R.id.txtEmail_RegisterActivity);
                userNameTxt.setError(Jsoup.parse(emailError).text());
                userNameTxt.requestFocus();
            }
        }
        if (userNameError!=null) {
            if (!userNameError.isEmpty()) {
                EditText userNameTxt=(EditText)findViewById(R.id.txtUserName_RegisterActivity);
                userNameTxt.setError(Jsoup.parse(userNameError).text());
                userNameTxt.requestFocus();
            }
        }
    }

    private void showClientValidationError(ClientSideValidationResult[] results){

        StringBuilder userNameSB=new StringBuilder();
        StringBuilder emailSB=new StringBuilder();

        for (ClientSideValidationResult result:results) {
            switch (result){
                case USERNAME_EMPTY:userNameSB.append(getResources().getString(R.string.field_empty_error)); break;
                case EMAIL_EMPTY:emailSB.append(getResources().getString(R.string.field_empty_error)); break;

                case USERNAME_ILLEGLE_CHAR_ERR:userNameSB.append(getResources().getString(R.string.username_contains_illeagle_char));break;
                case EMAIL_FORMAT_ERR:emailSB.append(getResources().getString(R.string.email_format_error));break;
            }
        }
        if (!userNameSB.toString().isEmpty()){
            userNameEditText.setError(userNameSB.toString());
        }
        if (!emailSB.toString().isEmpty()){
            emailEditText.setError(emailSB.toString());
        }
    }

    //Go back to login page
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
