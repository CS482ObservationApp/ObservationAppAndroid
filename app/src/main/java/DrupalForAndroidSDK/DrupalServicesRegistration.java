package DrupalForAndroidSDK;

/**
 * Created by zhuol on 2/18/2016.
 */
public class DrupalServicesRegistration extends DrupalServicesBase {
    public DrupalServicesRegistration(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("user/register");
    }
}
