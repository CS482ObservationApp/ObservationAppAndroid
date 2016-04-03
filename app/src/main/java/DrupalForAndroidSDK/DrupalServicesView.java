package DrupalForAndroidSDK;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

/**
 * Created by zhuol on 2/25/2016.
 */
public class DrupalServicesView extends DrupalServicesBase  {
    public enum ViewType {
        OBSERVATION_SEARCH,
        NEWEST_OBSERVATION,
        OBSERVATION_RECORD_AUTOCOMPLETE,
        SINGLE_NODE_DETAIL,
        PERSONAL_OBSERVATION
    }

    public DrupalServicesView(String baseURI, String endpoint) {
        super(baseURI, endpoint);
    }

    public HashMap<String,String> retrieve(ViewType viewType, BasicNameValuePair... params) throws Exception{
        switch (viewType){
            case OBSERVATION_SEARCH: setResource("search-mobile");break;
            case NEWEST_OBSERVATION: setResource("newest-observations-mobile");break;
            case OBSERVATION_RECORD_AUTOCOMPLETE: setResource("observation-record-autocomplete-mobile");break;
            case SINGLE_NODE_DETAIL: setResource("single-node-detail-mobile");break;
            case PERSONAL_OBSERVATION: setResource("my-posts");break;
        }
        return httpGetRequest(getURI(),params);
    }
}
