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
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import HelperClass.RegexValidator;

/*This Activity class is used to show a simple UI for getting a single text/number input
* It's used to get 2 types of input:
* Radius and Name which is used in Searching(for area radius)and User Profile(for user name updating)
* The caller activity could get the result back in onActivityResult()
 * */
public class ChangeTextFieldActivity extends Activity {

    //Intent key
    public static final String INTENT_RESULT="result";
    public static final String INTENT_TYPE ="type";
    //Maximum of searching area, the Circumference of Earth
    private final int MAX_RADIUS=40075;
    //May extend to other type
    public enum Type{
        USER_NAME,AREA_RADIUS
    }
    private Type type;
    private  EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_text_field);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Intent intent=getIntent();
        type=(Type)intent.getSerializableExtra(INTENT_TYPE);
        initializeVariables();
        initializeView();
        addOnClickListeners();
    }

    //Wrapped in onCreate()
    private void initializeVariables(){
    }
    private void initializeView(){
        switch (type){
            case USER_NAME:initializeChangeUserNameView();break;
            case AREA_RADIUS:initializeSetRadiusView();break;
        }
    }

    //Wrapped initializeView
    private void initializeChangeUserNameView(){
        editText=(EditText)findViewById(R.id.input_ChangeTextFieldActivity);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        editText.setHint(getText(R.string.hint_username));
    }
    private void initializeSetRadiusView(){
        editText=(EditText)findViewById(R.id.input_ChangeTextFieldActivity);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(getText(R.string.hint_radius));

        TextView hintTextView=(TextView)findViewById(R.id.hintTxt_ChangeTextFieldActivity);
        hintTextView.setText(getString(R.string.defalut_radius_changeTextFieldActivity));
    }

    private void addOnClickListeners(){
        setConfirmBtnOnClick();
        setLinearLayoutOnClick();
    }
    private void setConfirmBtnOnClick(){
        findViewById(R.id.btnConfirm_ChangeTextFieldActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editText.getText().toString();
                if (validateInputOnClient(input, type)){
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_RESULT, input);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
    private boolean validateInputOnClient(String input, Type type){
        switch (type) {
            case USER_NAME:
                return validateUserNameInputOnClient(input);
            case AREA_RADIUS:
                return validateSearchAreaRadiusInputOnClient(input);
        }
        return true;
    }
    private boolean validateUserNameInputOnClient(String input) {
        if (input.isEmpty()) {
            editText.setError(getText(R.string.field_empty_error));
            return false;
        } else if (!RegexValidator.validate(input, RegexValidator.InputType.USERNAME)) {
            editText.setError(getText(R.string.username_contains_illeagle_char));
            return false;
        }
        return true;
    }
    private boolean validateSearchAreaRadiusInputOnClient(String input) {
        if (input.isEmpty()) {
            editText.setError(getText(R.string.field_empty_error));
            return false;
        } else {
            try {
                if (MAX_RADIUS>Double.parseDouble(input)&&Double.parseDouble(input) > 0) {
                    return true;
                }else {
                    editText.setError(getText(R.string.invalid_number));
                    return false;
                }
            }catch (Exception e) {
                editText.setError(getText(R.string.invalid_number));
                return false;
            }
        }
    }
    private void setLinearLayoutOnClick(){
        findViewById(R.id.root_ll_changeTextField_ChangeTextFieldActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
            }
        });
    }
}
