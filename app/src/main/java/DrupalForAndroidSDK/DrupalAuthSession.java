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

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import DrupalForAndroidSDK.DrupalAuth;

/**
 * Created by keithyau on 12/12/13.
 */
public class DrupalAuthSession implements DrupalAuth {

    private String baseURI = "";
    private String endpoint = "";
    private String mSession = null;
    private String mToken = null;

    public void initAuth(String baseURI, String endpoint)
    {
        this.baseURI = baseURI;
        this.endpoint = endpoint;
    }

    public String getSession()
    {
        return mSession;
    }

    public void setSession(String _session)
    {
        mSession = _session;
    }


    private String getToken() throws Exception
    {
        if(mToken==null||mToken.isEmpty())
        {
            setToken();
        }
        return mToken;
    }

    private void setToken() throws Exception
    {
        HttpClient httpClient  = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(this.baseURI + "/services/session/token");

        String cookie=getSession();
        if (cookie!=null) {
            httpGet.setHeader("Cookie",cookie );
        }
        HttpResponse token = httpClient.execute(httpGet);
        mToken =  EntityUtils.toString(token.getEntity());
    }

    @Override
    public <T extends HttpRequestBase> T initRequest(T request) throws Exception
    {
        //Refresh X-XSRF-Token before request
        setToken();

        //Set token and session to header
        if (getToken() != null) {
            request.setHeader("X-CSRF-Token", getToken());
        }
        if (getSession() != null) {
            request.setHeader("Cookie", getSession());
        }

        return request;
    }
}
