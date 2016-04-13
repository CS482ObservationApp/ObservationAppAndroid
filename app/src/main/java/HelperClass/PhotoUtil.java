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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.zhuoliupei.observationapp.ChooseUploadPhotoMethodActivity;


/**
 * Created by zhuol on 3/6/2016.
 */
public class PhotoUtil {
    public static void launchGalleryAppForPhoto(Activity context,int requestID){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        context.startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), requestID);
    }
    public static void launchCameraForPhoto(Activity context,int requestID){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        context.startActivityForResult(cameraIntent, requestID);
    }

    public static Bitmap getBitmapFromLocalUri(Uri uri, Context context) {
        try {
            // Let's read picked image path using content resolver
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex) {
            return null;
        }
    }
    public static Bitmap getBitmapFromFile(File file,int maxSize){
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);
            int scale=1;
            while (o.outWidth/scale/2>=maxSize&&o.outHeight/scale/2>=maxSize){
                scale*=2;
            }
            BitmapFactory.Options o2=new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(file),null,o2);
        }catch (FileNotFoundException e){}
        return  null;
    }
    public static Bitmap getBitmapFromServerURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
    public static void startPickingPhoto(Activity context,int result,int requestID) {
        switch (result) {
            case ChooseUploadPhotoMethodActivity.CHOOSE_PHOTO_FROM_CAMERA: {
                PhotoUtil.launchCameraForPhoto(context, requestID);
                break;
            }
            case ChooseUploadPhotoMethodActivity.CHOOSE_PHOTO_FROM_GALARY: {
                PhotoUtil.launchGalleryAppForPhoto(context, requestID);
                break;
            }
        }
    }

    public static Bitmap reduceBitMapSize(Bitmap bitmap,int maxSize){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (!(width*height>=maxSize))
            return bitmap;
        int ratio=(int)Math.ceil(Math.sqrt(width*height/maxSize));
        int newWidth=(int)(width/ratio);
        int newHeight=(int)(height/ratio);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
