package DrupalForAndroidSDK;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import DrupalForAndroidSDK.DrupalServicesBase;

/**
 * Created by zhuol on 2/18/16.
 */
//To-do support session auth
public class DrupalServicesFilterView extends DrupalServicesBase  {

    public DrupalServicesFilterView(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("newest-observation-view");
}
    public String[] retrive() {
        return this.httpGetRequest(this.getURI());
    }
}
