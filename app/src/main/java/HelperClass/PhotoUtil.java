package HelperClass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;


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

    public static Bitmap getBitmapFromUri(Uri uri,Context context) {
        try {
            // Let's read picked image path using content resolver
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex) {
            return null;
        }
    }
}
