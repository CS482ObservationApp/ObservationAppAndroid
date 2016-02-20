package DrupalForAndroidSDK;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by zhuol on 2/18/2016.
 */
public class DrupalServicesNewestView extends DrupalServicesBase  {

    public DrupalServicesNewestView(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("filter-view");
    }

    public String[] retrieve(BasicNameValuePair[] params) {
        return this.httpGetRequest(this.getURI(),params);
    }

    public String[] index() {
        return this.httpGetRequest(this.getURI());
    }
}
