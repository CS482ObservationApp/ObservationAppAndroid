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

import android.graphics.Bitmap;
import android.util.Base64;

import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;

/**
 * Created by zhuol on 3/6/2016.
 */
public class UploadUtil {
    public static String getBase64StringFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    public static BasicNameValuePair[] constructBasicFileUploadParams(String filename, String fileServerPath, String encoded){
        BasicNameValuePair[] params = new BasicNameValuePair[3];
        params[0]=new BasicNameValuePair("file[file]",encoded);
        params[1]=new BasicNameValuePair("file[filename]",filename);
        params[2]=new BasicNameValuePair("file[filepath]",fileServerPath);
        return params;
    }
}
