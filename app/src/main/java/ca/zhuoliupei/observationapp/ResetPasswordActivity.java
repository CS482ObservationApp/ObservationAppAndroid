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
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import Const.DrupalServicesFieldKeysConst;
import java.util.HashMap;

import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesUser;
import HelperClass.RegexValidator;
import HelperClass.ToolBarStyler;
/**This class provide functions:
 * 1. Provide UI to get the user's email
 * 2. Send a reset password request to server for the entered email
 * 3. Return if the request is success or not*/
public class ResetPasswordActivity extends AppCompatActivity {

    EditText inputField;
    String inputText;
    String baseUrl,endpoint;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initializeVariables();
        initializeUI();
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
        toolbar=(Toolbar)findViewById(R.id.toolbar_ResetPassword);
    }

    private void initializeUI(){
        initializeToolBar();
    }
    private void initializeToolBar(){
        if (toolbar!=null)
            ToolBarStyler.styleToolBar(this, toolbar, "Reset Password");
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
            String statusCode=results.get(DrupalServicesFieldKeysConst.STATUS_CODE);
            try{
                if (statusCode.equals(HTTPConst.HTTP_OK_200)){
                    ((TextView)findViewById(R.id.txtMessage_RestPasswordActivity)).setText(getResources().getString(R.string.reset_successful));
                }else if(statusCode.equals(HTTPConst.HTTP_UNAUTHORIZED_401)||statusCode.equals(HTTPConst.HTTP_UOT_ACCEPT_406)){
                    ((TextView)findViewById(R.id.txtMessage_RestPasswordActivity)).setText(Jsoup.parse(results.get(DrupalServicesFieldKeysConst.RESPONSE_BODY)).text() );
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


}
