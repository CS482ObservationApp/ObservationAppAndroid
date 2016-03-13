package HelperClass;

import android.content.Context;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Const.DrupalServicesResponseConst;
import Const.HTTPConst;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesView;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/12/2016.
 */
public class ObservationRecordFinder {
    Context context;

    public ObservationRecordFinder(Context context){
        this.context=context;
    }
    public List<String> findRecords(String name){
        DrupalAuthSession authSession=new DrupalAuthSession();
        authSession.setSession(PreferenceUtil.getCookie(context));
        String baseUrl= context.getText(R.string.drupal_site_url).toString();
        String endpoint=context.getText(R.string.drupal_server_endpoint).toString();

        DrupalServicesView drupalServicesView=new DrupalServicesView(baseUrl,endpoint);
        drupalServicesView.setAuth(authSession);
        BasicNameValuePair[] params=new BasicNameValuePair[1];
        params[0]=new BasicNameValuePair("name",name);
        try {
            HashMap<String,String> responseMap= drupalServicesView.retrive(DrupalServicesView.View.OBSERVATION_RECORD_AUTOCOMPLETE, params);
            if (responseMap.get(DrupalServicesResponseConst.STATUS_CODE).equals(HTTPConst.HTTP_OK_200)){
                return getNameListFromJson(responseMap.get(DrupalServicesResponseConst.RESPONSE_BODY));
            }else {
                return new ArrayList<>();
            }
        }catch (Exception ex){
            return new ArrayList<>();
        }
    }
    private List<String> getNameListFromJson(String jsonStr){
        try {
            ArrayList<String> titleList=new ArrayList<>();
            JSONArray titleArray=new JSONArray(jsonStr);
            for (int i=0;i<titleArray.length();i++ ){
                JSONObject titleObject=titleArray.getJSONObject(i);
                titleList.add(titleObject.getString("title"));
            }
            return  titleList;
        }catch (Exception ex){
            return new ArrayList<>();
        }
    }
}