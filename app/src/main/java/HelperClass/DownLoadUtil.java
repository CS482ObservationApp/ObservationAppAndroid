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
