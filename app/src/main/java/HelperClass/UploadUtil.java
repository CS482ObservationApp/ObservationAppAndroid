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
