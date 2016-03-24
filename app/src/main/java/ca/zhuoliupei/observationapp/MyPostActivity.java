package ca.zhuoliupei.observationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import Adapter.MyPostAdapter;

public class MyPostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        initializeVariables();
        initializeUI();
    }
    private void initializeVariables(){

    }
    private void initializeUI(){
        ListView listView=((ListView)findViewById(R.id.content_lv_MyPostActivity));
        listView.setAdapter(new MyPostAdapter(this));
    }
}
