package ca.zhuoliupei.observationapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import Const.DrupalServicesResponseConst;
import java.util.HashMap;

import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.RegexValidator;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText inputField;
    String inputText;
    String baseUrl,endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initializeVariables();

        findViewById(R.id.imgBtnSubmit_ResetPasswordActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()){
                    sendResetPasswordRequest(inputText);
                }
            }
        });
    }
    private void initializeVariables() {
        baseUrl=getResources().getString(R.string.drupal_site_url);
        endpoint=getResources().getString(R.string.drupal_server_endpoint);
    }

    private boolean validateInput(){
        inputField=(EditText)findViewById(R.id.txtUsernameEmail_ResetPasswordActivity);
        inputText=inputField.getText().toString().trim();
        if (inputText.isEmpty()){
            inputField.setError(getResources().getString(R.string.field_empty_error));
            return false;
        }else if (RegexValidator.validate(inputText, RegexValidator.InputType.USERNAME)||RegexValidator.validate(inputText, RegexValidator.InputType.EMAIL)){
            return true;
        }
        inputField.setError(getResources().getString(R.string.non_valid_name_email));
        return false;
    }

    private class ResetPasswordTask extends AsyncTask<String,Void,HashMap<String,String>>{
        Context context;

        public ResetPasswordTask(Context context){
            this.context=context;
        }
        @Override
        protected HashMap<String,String> doInBackground(String... input) {
            DrupalServicesUser drupalServicesUser =new DrupalServicesUser(baseUrl,endpoint);
            drupalServicesUser.setAuth(new DrupalAuthSession());
            try{
                return drupalServicesUser.resetPassword(input[0]);
            }catch (Exception e){
                return  new HashMap<>();
            }
        }

        @Override
        protected void onPostExecute(HashMap<String,String> results) {
            String statusCode=results.get(DrupalServicesResponseConst.STATUS_CODE);
            try{
                if (statusCode.equals(HTTPConst.HTTP_OK_200)){
                    ((TextView)findViewById(R.id.txtMessage_RestPasswordActivity)).setText(getResources().getString(R.string.reset_successful));
                }else if(statusCode.equals(HTTPConst.HTTP_UNAUTHORIZED_401)||statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)){
                    ((TextView)findViewById(R.id.txtMessage_RestPasswordActivity)).setText(Jsoup.parse(results.get(DrupalServicesResponseConst.RESPONSE_BODY)).text() );
                }else {
                    throw new Exception();
                }
            }catch (Exception e){
                Toast.makeText(context,getText(R.string.network_error), Toast.LENGTH_LONG).show();
            }
        }
    }
    private void sendResetPasswordRequest(String input){
        new ResetPasswordTask(this).execute(input);
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,LoginActivity.class));
    }
}
