package DrupalForAndroidSDK;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

import DrupalForAndroidSDK.DrupalServicesBase;

/**
 * Created by jimmyko on 10/13/13.
 */
//To-do support session auth
public class DrupalServicesNode extends DrupalServicesBase implements DrupalServicesResource {


    //To-do a method for annoynmous consuming resource

    public DrupalServicesNode(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("node");
    }

    @Override
    public HashMap<String,String> create(BasicNameValuePair[] params)throws Exception {
        return this.httpPostRequest(this.getURI(), params);
    }

    @Override
    public HashMap<String,String> retrieve(int id)throws Exception {
        return this.httpGetRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> update(int id, BasicNameValuePair[] params)throws Exception {
        return this.httpPutRequest(this.getURI() + "/" + id, params);
    }

    @Override
    public HashMap<String,String> delete(int id) throws Exception{
        return this.httpDeleteRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> index()throws Exception {
        return this.httpGetRequest(this.getURI());
    }

}
