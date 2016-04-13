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

import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Const.DrupalServicesFieldKeysConst;

/**
 * Created by jimmyko on 10/13/13.
 */
public class DrupalServicesBase {

    private String baseURI = "";
    private String endpoint = "rest";
    protected String resource = "";
    private DrupalAuth auth;

    private List<NameValuePair> pairsToSend = new ArrayList<NameValuePair>();

    public DrupalServicesBase(String baseURI, String endpoint) {
        this.baseURI = baseURI;
        this.endpoint = endpoint;
    }


    public void setAuth(DrupalAuth auth) {
        this.auth = auth;
        auth.initAuth(baseURI, endpoint);
    }

    protected void setResource (String resource) {
        this.resource = resource;
    }

    protected String getURI() {
        return this.baseURI + "/" + this.endpoint + "/" + this.resource;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public HashMap<String,String> httpGetRequest (String uri) throws Exception {
        HttpGet request = new HttpGet(uri);
        return httpSendRequest(request);
    }

    // Only GET request contains query parameters.
    public HashMap<String,String>  httpGetRequest (String uri, BasicNameValuePair[] params) throws Exception
    {
        for (int i = 0; i < params.length; i++) {
            pairsToSend.add(params[i]);
        }

        Uri.Builder uriBuilder = Uri.parse(uri).buildUpon();
        for (NameValuePair param : pairsToSend) {
            uriBuilder.appendQueryParameter(param.getName(), param.getValue());
        }
        uri = uriBuilder.build().toString();
        HttpGet request = new HttpGet(uri);
        return httpSendRequest(request);
    }

    public HashMap<String,String> httpPostRequest ( String url) throws Exception {
        HttpPost request = new HttpPost(getURI());

        return httpSendRequest(request);
    }

    public HashMap<String,String> httpPostRequest (String uri, BasicNameValuePair[] params)  throws Exception{
        HttpPost request = new HttpPost(uri);

        for (int i = 0; i < params.length; i++) {
            pairsToSend.add(params[i]);
        }
        // assign parameters to request
        try {
            request.setEntity(new UrlEncodedFormEntity(pairsToSend));
            System.out.println("the request is: " + request.getURI() + EntityUtils.toString(request.getEntity()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpSendRequest(request);
    }

    public HashMap<String,String> httpDeleteRequest (String uri) throws Exception
    {
        HttpDelete request = new HttpDelete(uri);
        return httpSendRequest(request);
    }

    public HashMap<String,String> httpPutRequest (String uri, BasicNameValuePair[] params) throws Exception
    {
        HttpPut request = new HttpPut(uri);

        for (int i = 0; i < params.length; i++) {
            pairsToSend.add(params[i]);
        }

        // assign parameters to request
        try {
            request.setEntity(new UrlEncodedFormEntity(pairsToSend));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpSendRequest(request);
    }

    private <T extends HttpRequestBase> HashMap<String,String> httpSendRequest (T request) throws Exception{
        this.auth.initRequest(request);

        // set header
        request.setHeader("Accept", "application/json");
        request.setHeader("content-type", "application/x-www-form-urlencoded");

        // send the request
        HttpClient client = new DefaultHttpClient();

            HttpResponse response = client.execute(request);
            // if successful, return the response body
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String responseBody = EntityUtils.toString(resEntity);
                HashMap<String, String> responseMap = new HashMap<>();
                responseMap.put(DrupalServicesFieldKeysConst.STATUS_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
                responseMap.put(DrupalServicesFieldKeysConst.RESPONSE_BODY, responseBody);

                return responseMap;
            }
            HashMap<String, String> responseMap = new HashMap<>();
            responseMap.put(DrupalServicesFieldKeysConst.STATUS_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
            return responseMap;
    }
    
}

