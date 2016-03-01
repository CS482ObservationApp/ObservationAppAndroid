package DrupalForAndroidSDK;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by keithyau on 12/12/13.
 */
public interface DrupalAuth {
     void initAuth(String baseURI, String endpoint) ;

     <T extends HttpRequestBase> T initRequest (T request) throws Exception;
}