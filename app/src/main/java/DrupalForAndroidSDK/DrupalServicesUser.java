package DrupalForAndroidSDK;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

/**
 * Created by zhuol on 2/21/2016.
 */
public class DrupalServicesUser extends DrupalServicesBase{

    private String baseUrl,endpoint;

;    public DrupalServicesUser(String baseUrl, String endpoint){
        super(baseUrl,endpoint);
    }

    public HashMap<String,String> login(String username,String password) throws Exception
    {
        setResource("user/login");

        BasicNameValuePair[] params=new BasicNameValuePair[2];
        params[0]= new BasicNameValuePair("username", username );
        params[1]= new BasicNameValuePair("password", password);
        return httpPostRequest(getURI(),params);
    }


    public HashMap<String,String> register(String username,String email) throws Exception{
        setResource("user/register");
        BasicNameValuePair[] params=new BasicNameValuePair[2];
        params[0]=new BasicNameValuePair("name",username);
        params[1]=new BasicNameValuePair("mail",email);
        return httpPostRequest(getURI(),params);
    }

    public HashMap<String,String> logout(Context context) throws Exception{
            setResource("user/logout");
            return httpPostRequest(getURI(),new BasicNameValuePair[0]);
    }

    public HashMap<String,String> resetPassword(String identity) throws Exception{
        setResource("user/request_new_password");
        BasicNameValuePair[] params=new BasicNameValuePair[1];
        params[0]=new BasicNameValuePair("name",identity);
        return httpPostRequest(getURI(),params);
    }

    public HashMap<String,String> getUser(int userId) throws Exception{
        setResource("user/"+userId);
        return httpGetRequest(getURI());
    }

    public HashMap<String, String> getSessionInfo() throws Exception {
        setResource("system/connect");
        return httpPostRequest(getURI());
    }

}


//        if (responseHashHashMap.get(DrupalServicesBase.STATUSCODE).equals("200")) {
//            JSONObject responseJsonObject = new JSONObject(responseHashHashMap.get(DrupalServicesBase.RESPONSEBODY));
//            JSONObject userJsonObject = responseJsonObject.getJSONObject("user");
//            JSONObject rolesJsonObject = userJsonObject.getJSONObject(ROLES);
//
//            String sessionid = responseJsonObject.getString(SESSIONID);
//            String sessionname = responseJsonObject.getString(SESSIONNAME);
//            String userid = userJsonObject.getString(USERID);
//            String rolesStr = "";
//            HashMap<String, String> userInfoMap = new HashMap<>();
//
//            Iterator<String> keys = rolesJsonObject.keys();
//
//            while (keys.hasNext()) {
//                String key = keys.next();
//                rolesStr += rolesJsonObject.getString(key) + ",";
//            }
//
//            userInfoMap.put(DrupalServicesBase.STATUSCODE, responseHashHashMap.get(DrupalServicesBase.STATUSCODE));
//            userInfoMap.put(ROLES, rolesStr);
//            userInfoMap.put(SESSIONID, sessionid);
//            userInfoMap.put(USERID, userid);
//            userInfoMap.put(SESSIONNAME, sessionname);
//
//            return userInfoMap;
//        }