package HelperClass;

import android.content.Context;
import android.preference.PreferenceManager;

import Const.SharedPreferencesConst;

/**
 * Created by zhuol on 3/2/2016.
 */
public class PreferenceUtil {

    static String EMPTY_STRING="";
    /*************************  Read  *************************/
    public static boolean getCurrentUserStatus(Context context) {
        //If the user login before,his/her info would be in Shared Preferences, otherwise return false
        String currentUser= PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_NAME,EMPTY_STRING);
        boolean sessionExpired=PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SharedPreferencesConst.K_SESSION_EXPIRED, true);
        return currentUser != null&&!sessionExpired;
    }
    public static String getCurrentUser(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_NAME,EMPTY_STRING);
    }
    public static String getCurrentUserLocation1(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_ADDRESS_1,EMPTY_STRING);
    }
    public static String getCurrentUserLocation2(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_ADDRESS_2,EMPTY_STRING);
    }
    public static String getCurrentUserPictureLocalUri(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_IMAGE_LOCAL_URI,EMPTY_STRING);
    }
    public static String getCurrentUserPictureServerUri(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_IMAGE_SERVER_URI,EMPTY_STRING);
    }
    public static String getCookie(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_COOKIE,EMPTY_STRING);
    }
    public static String getCurrentUserEmail(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_EMAIL,EMPTY_STRING);
    }



    /*****************************  Write *************************/
    public static void saveString(Context context,String key,String value){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
    }
    public static void saveBoolean(Context context,String key,boolean value){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
    }
}
