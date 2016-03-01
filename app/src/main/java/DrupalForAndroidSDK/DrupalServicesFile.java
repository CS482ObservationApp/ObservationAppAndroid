package DrupalForAndroidSDK;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

/**
 * Created by keithyau on 11/7/13.
 */

public class DrupalServicesFile extends DrupalServicesBase implements DrupalServicesResource {


    public DrupalServicesFile(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("file");
    }

    @Override
    public HashMap<String,String> create(BasicNameValuePair[] params) throws Exception {
        return this.httpPostRequest(this.getURI(), params);
    }

    @Override
    public HashMap<String,String> retrieve(int id) throws Exception{
        return this.httpGetRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> update(int id, BasicNameValuePair[] params) throws Exception{
        return this.httpPutRequest(this.getURI() + "/" + id, params);
    }

    @Override
    public HashMap<String,String> delete(int id) throws Exception {
        return this.httpDeleteRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> index() throws Exception {
        return this.httpGetRequest(this.getURI());
    }
}