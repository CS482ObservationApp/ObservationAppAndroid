package DrupalForAndroidSDK;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jimmyko on 10/13/13.
 */
public interface DrupalServicesResource {

    public HashMap<String,String> create(BasicNameValuePair[] params) throws Exception;

    public HashMap<String,String> retrieve(int id) throws Exception;

    public HashMap<String,String> update(int id, BasicNameValuePair[] params) throws Exception;

    public HashMap<String,String> delete(int id) throws Exception;

    public HashMap<String,String> index() throws Exception;
}
