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
        BasicNameValuePair[] params=new BasicNameValuePair[3];
        params[0]=new BasicNameValuePair("name",username);
        params[1]=new BasicNameValuePair("mail",email);
        params[2]=new BasicNameValuePair("legal_accept","Accept");
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

    public HashMap<String,String> update(BasicNameValuePair[] params,String userID) throws Exception{
        setResource("user/"+userID);
        return httpPutRequest(getURI(),params);
    }

}


