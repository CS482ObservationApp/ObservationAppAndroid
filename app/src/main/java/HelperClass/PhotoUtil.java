package HelperClass;

import android.app.Activity;
import android.content.Intent;

import Const.RequestIDConst;

/**
 * Created by zhuol on 3/6/2016.
 */
public class PhotoUtil {
    public static void launchGalleryAppForPhoto(Activity context){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        context.startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), RequestIDConst.GET_PHOTO_REQUEST);
    }
    public static void launchCameraForPhoto(Activity context){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        context.startActivityForResult(cameraIntent, RequestIDConst.GET_PHOTO_REQUEST);
    }
}
