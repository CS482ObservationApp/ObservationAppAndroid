package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;

import DrupalForAndroidSDK.DrupalServicesRegistration;
import ValidationHelpers.RegexValidator;

public class RegisterActivity extends AppCompatActivity {

    private enum ClientSideValidationResult{
        VALIDATION_PASS,
        USERNAME_ILLEGLE_CHAR_ERR,  USERNAME_EMPTY,
        EMAIL_FORMAT_ERR,           EMAIL_EMPTY,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Add link into EditText
        TextView useTermEditText=(TextView)findViewById(R.id.txtAgreeTerm_RegisterActivity);
        if (useTermEditText != null) {
            useTermEditText.setMovementMethod(LinkMovementMethod.getInstance()); //"http://stackoverflow.com/questions/2734270/how-do-i-make-links-in-a-EditText-clickable"
        }

        //CheckBox onchecked
        CheckBox agreeTermChkbox = (CheckBox) findViewById(R.id.chkbxAgreeTerm_RegisterActivity);
        agreeTermChkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ImageButton registerBtn=(ImageButton)findViewById(R.id.imgBtnRegister_RegisterActivity);
                if (isChecked){
                    registerBtn.setEnabled(true);
                    registerBtn.setImageResource(R.drawable.register_button_enabled);
                }else{
                    registerBtn.setEnabled(false);
                    registerBtn.setImageResource(R.drawable.register_button_disabled);
                }
            }
        });

        //Register Button onclick
        ImageButton registerBtn=(ImageButton)findViewById(R.id.imgBtnRegister_RegisterActivity);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            showValidationError(clientSideValidationResults.toArray(new ClientSideValidationResult[clientSideValidationResults.size()]));
        }
    }
    private ArrayList<ClientSideValidationResult> validateFormOnClient(){
        ArrayList<ClientSideValidationResult> results=new ArrayList<ClientSideValidationResult>();

        String userNameStr=((EditText)findViewById(R.id.txtUserName_RegisterActivity)).getText().toString().trim();
        String emailStr=((EditText)findViewById(R.id.txtEmail_RegisterActivity)).getText().toString().trim();

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

    private class RegisterAccountTask extends AsyncTask<String,Void,String[]>{
        protected String[] doInBackground(String... strings){
            String usernameStr=strings[0];
            String emailStr=strings[1];

            String baseUrl=getResources().getString(R.string.drupal_site_url);
            String endpoint=getResources().getString(R.string.drupal_server_endpoint);

            DrupalServicesRegistration drupalServicesRegistration=new DrupalServicesRegistration(baseUrl,endpoint);
            BasicNameValuePair[] params=new BasicNameValuePair[2];
            params[0]=new BasicNameValuePair("name",usernameStr);
            params[1]=new BasicNameValuePair("mail",emailStr);
            return drupalServicesRegistration.httpPostRequest(params);
        }

        protected void onPostExecute(String[] response){
            handleHttpResponse(response);
        }
    }
    private void postRegisterInfo(){
        String username=((EditText)findViewById(R.id.txtUserName_RegisterActivity)).getText().toString().trim();
        String email=((EditText)findViewById(R.id.txtEmail_RegisterActivity)).getText().toString().trim();

        RegisterAccountTask registerAccountTask=new RegisterAccountTask();
        registerAccountTask.execute(username,email);
    }

    private void handleHttpResponse(String[] response){
        int statusCode = Integer.parseInt(response[0]);
        String responseStr=response[1];
        try{
            if(statusCode==200){
                TextView msgTextView = (TextView)findViewById(R.id.txtMessage_RegisterActivity);
                msgTextView.setText(getResources().getString(R.string.register_successful));
            }else if (statusCode==406){
                JSONObject responseJsonObject=new JSONObject(responseStr);
                JSONObject errorJsonObject=responseJsonObject.getJSONObject("form_errors");
                String userNameError=errorJsonObject.getString("name");
                String emailError=errorJsonObject.getString("mail");
                if (!userNameError.isEmpty()){
                    ((EditText)findViewById(R.id.txtUserName_RegisterActivity)).setError(Jsoup.parse(userNameError).text());
                }
                if (!emailError.isEmpty()){
                    ((EditText)findViewById(R.id.txtEmail_RegisterActivity)).setError(Jsoup.parse(emailError).text());
                }
            }else{
                throw new Exception("unknown response");
            }
        }catch (Exception e){
            Toast.makeText(this,getResources().getString(R.string.network_error),Toast.LENGTH_LONG);
        }

    }

    private void showValidationError(ClientSideValidationResult[] results){
        EditText userNameEditText = (EditText) findViewById(R.id.txtUserName_RegisterActivity);
        EditText emailEditText = (EditText) findViewById(R.id.txtEmail_RegisterActivity);

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
