package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import HelperClass.RegexValidator;

public class ChangeTextFieldActivity extends Activity {

    //May extend to other type
    public enum Type{
        USER_NAME
    }
    private Type type;
    private  EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_text_field);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Intent intent=getIntent();
        int typeIndex=intent.getIntExtra("type",0);
        type=Type.values()[typeIndex];
        initializeVariables();
        initializeView();
        addOnClickListeners();
    }
    private void initializeVariables(){
    }
    private void initializeView(){
        switch (type){
            case USER_NAME:initializeChangeUserNameView();break;
        }
    }
    private void initializeChangeUserNameView(){
        editText=(EditText)findViewById(R.id.input_ChangeTextFieldActivity);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        editText.setHint(getText(R.string.hint_username));
    }
    private void addOnClickListeners(){
        setConfirmBtnOnClick();
        setEditTextOnClick();
        setLinearLayoutOnClick();
    }
    private void setConfirmBtnOnClick(){
        findViewById(R.id.btnConfirm_ChangeTextFieldActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = editText.getText().toString();
                if (input.isEmpty()) {
                    editText.setError(getText(R.string.field_empty_error));
                } else if (!RegexValidator.validate(input, RegexValidator.InputType.USERNAME)) {
                    editText.setError(getText(R.string.username_contains_illeagle_char));
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("result", input);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
    private void setEditTextOnClick(){
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
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
