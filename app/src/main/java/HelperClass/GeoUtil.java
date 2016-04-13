/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package HelperClass;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.provider.Telephony;
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

    public static String getFullAddress(Address address){
        String fullAddress="";
        if (address.getFeatureName()!=null&& !address.getFeatureName().isEmpty())
            fullAddress+=address.getFeatureName() +",";
        if (address.getThoroughfare()!=null&& !address.getThoroughfare().isEmpty())
            fullAddress+=address.getThoroughfare()+",";
        if (address.getLocality()!=null&& !address.getLocality().isEmpty())
            fullAddress+=address.getLocality()+",";
        if (address.getSubAdminArea()!=null&& !address.getSubAdminArea().isEmpty())
            fullAddress+=address.getSubAdminArea()+",";
        if (address.getAdminArea()!=null&& !address.getAdminArea().isEmpty())
            fullAddress+=address.getAdminArea()+",";
        if (address.getCountryCode()!=null&& !address.getCountryCode().isEmpty())
            fullAddress+=address.getCountryCode()+",";
        if (address.getPostalCode()!=null&& !address.getPostalCode().isEmpty())
            fullAddress+=address.getPostalCode() +",";
        if (!fullAddress.isEmpty())
            fullAddress=fullAddress.substring(0,fullAddress.length()-1);
        return fullAddress;
    }
}
