package ca.zhuoliupei.observationapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import javax.xml.validation.Validator;

import ValidationHelpers.RegexValidator;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                if (validateLoginInfo()){
                    if (loginUser()){
                        Intent intent=new Intent(LoginActivity.this,NewestObservationsActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private boolean validateLoginInfo(){
        boolean validate=true;

        EditText emailEditText=((EditText)findViewById(R.id.textEmail_LoginActivity));
        EditText passwordEditText=((EditText)findViewById(R.id.textPassword_LoginActivity));
        String emailStr=emailEditText.getText().toString().trim();
        String passwordStr=passwordEditText.getText().toString().trim();

        if (emailStr.isEmpty()){
            emailEditText.setError(getResources().getString(R.string.field_empty_error));
            validate=false;
        }else if (!RegexValidator.validate(emailStr, RegexValidator.InputType.EMAIL))
        {
            emailEditText.setError(getResources().getString(R.string.email_format_error));
            validate=false;
        }

        if (passwordStr.isEmpty()) {
            passwordEditText.setError(getResources().getString(R.string.field_empty_error));
            validate = false;
        }

        return validate;
    }

    private boolean loginUser(){
        //TODO:
        return  true;
    }
}
