package ca.zhuoliupei.observationapp;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import Model.SerializableNameValuePair;

public class SearchResultActivity extends AppCompatActivity {

    BasicNameValuePair[] searchParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        acceptIntentExtras();
    }
    private void acceptIntentExtras(){
        Bundle extras = getIntent().getExtras();
        ArrayList<SerializableNameValuePair> paramList  =  (ArrayList<SerializableNameValuePair>) extras.getSerializable("params");
        searchParams=getParams(paramList);
    }



    /******Helper Methods*******/
    private BasicNameValuePair[] getParams(ArrayList<SerializableNameValuePair> paramList){
        BasicNameValuePair[] params=new BasicNameValuePair[paramList.size()];
        for (int i=0;i<paramList.size();i++){
            params[i]=new BasicNameValuePair(paramList.get(i).getName(),paramList.get(i).getValue());
        }
        return params;
    }
}
