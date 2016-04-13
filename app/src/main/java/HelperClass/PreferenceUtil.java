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
        return currentUser!=null &&!currentUser.equals(EMPTY_STRING)&&!sessionExpired;
    }
    public static String getCurrentUserID(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferencesConst.K_USER_ID,EMPTY_STRING);
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

    /*****************************  Delete *************************/
    public static void deleteKey(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).commit();
    }
}
