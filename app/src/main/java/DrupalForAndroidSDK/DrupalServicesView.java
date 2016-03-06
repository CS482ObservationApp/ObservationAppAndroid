package DrupalForAndroidSDK;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

/**
 * Created by zhuol on 2/25/2016.
 */
public class DrupalServicesView extends DrupalServicesBase  {
    public enum View{
        OBSERVATION_SEARCH,NEWEST_OBSERVATION
    };

    public DrupalServicesView(String baseURI, String endpoint) {
        super(baseURI, endpoint);
    }

    public HashMap<String,String> retrive(View view,BasicNameValuePair... params) throws Exception{
        switch (view){
            case OBSERVATION_SEARCH: setResource("observation-search-mobile");break;
            case NEWEST_OBSERVATION: setResource("newest-observations-mobile");break;
        }
        return httpGetRequest(getURI(),params);
    }
}
