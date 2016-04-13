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
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by zhuol on 3/3/2016.
 */
public class DownLoadUtil {
    public static boolean downloadImage(String imgServerUrl,String pathToSave){
        try {
            URL url = new URL(imgServerUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            FileOutputStream out = new FileOutputStream(pathToSave);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return true;
        }catch (Exception e){
            Log.e("IMAGE_DOWNLOAD", "Cannot download image");
            return false;
        }
    }

    public static boolean downloadImage(String imgServerUrl,String pathToSave,Bitmap.CompressFormat format,int quality){
        try {
            URL url = new URL(imgServerUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            FileOutputStream out = new FileOutputStream(pathToSave);
            bmp.compress(format, quality, out);
            return true;
        }catch (Exception e){
            Log.e("IMAGE_DOWNLOAD", "Cannot download image");
            return false;
        }
    }
}
