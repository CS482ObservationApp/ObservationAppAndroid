package Model;


/**
 * Created by zhuol on 3/11/2016.
 */
public class DrupalAddress {
    public String country;
    public String localicity;
    public String adminArea;
    public String thoroug;
    public String postalCode;

    public DrupalAddress(String country,String adminArea,String localicity,String thoroug,String postalCode){
        this.country=country;
        this.adminArea=adminArea;
        this.localicity=localicity;
        this.thoroug=thoroug;
        this.postalCode=postalCode;
    }
}
