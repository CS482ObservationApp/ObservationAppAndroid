package HelperClass;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/11/2016.
 */
public class GeoUtil {
    enum ReverseGeoCodingResult{
        IO_ERROR,INVALID_LAT_LON,NO_ADDRESS_AVAILABLE,ADDRESS_FOUND
    }
    public static ReverseGeoCodingResult reverseGeoCode(double lat,double lon,Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String errorMessage = "";
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException ioException) {
            return ReverseGeoCodingResult.IO_ERROR;
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            return ReverseGeoCodingResult.INVALID_LAT_LON;
        }
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            return ReverseGeoCodingResult.NO_ADDRESS_AVAILABLE;
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            return ReverseGeoCodingResult.ADDRESS_FOUND;
        }
    }
}
