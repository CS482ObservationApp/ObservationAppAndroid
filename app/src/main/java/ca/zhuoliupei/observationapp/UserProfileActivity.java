package ca.zhuoliupei.observationapp;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import HelperClass.PreferenceUtil;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        initializeView();
    }

    private void initializeView(){
        ImageView profileImageIV=(ImageView)findViewById(R.id.imgUserImage_UserProfileActivity);
        TextView userNameTV=(TextView)findViewById(R.id.txtUserName_UserProfileActivity);
        TextView emailTV=(TextView)findViewById(R.id.txtEmail_UserProfileActivity);
        TextView address1TV=(TextView)findViewById(R.id.txtAddress1_UserProfileActivity);
        TextView address2TV=(TextView)findViewById(R.id.txtAddress2_UserProfileActivity);

        String imgFilePath=PreferenceUtil.getCurrentUserPictureLocalUri(this);
        String userName=PreferenceUtil.getCurrentUser(this);
        String email=PreferenceUtil.getCurrentUserEmail(this);
        String address1=PreferenceUtil.getCurrentUserLocation1(this);
        String address2=PreferenceUtil.getCurrentUserLocation2(this);

        File imgFile = new File(imgFilePath);
        if (imgFile.exists())
            profileImageIV.setImageURI(Uri.parse(imgFilePath));
        else profileImageIV.setImageResource(R.drawable.icon_user_default);

        userNameTV.setText(userName);
        emailTV.setText(email);
        address1TV.setText(address1);
        address2TV.setText(address2);
    }
}
