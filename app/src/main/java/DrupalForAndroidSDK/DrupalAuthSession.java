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
