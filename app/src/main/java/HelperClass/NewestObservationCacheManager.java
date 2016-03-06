package HelperClass;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.HeaderViewListAdapter;

import java.io.Console;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import Const.DrupalServicesResponseConst;
import DBContract.ObservationContract;
import DBHelper.NewestObservationDBHelper;
import DrupalForAndroidSDK.DrupalAuthSession;
import DrupalForAndroidSDK.DrupalServicesNode;
import DrupalForAndroidSDK.DrupalServicesUser;
import Model.ObservationObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationCacheManager {
    String cacheDir;
    Activity context;
    int maxCacheAmount;

    public NewestObservationCacheManager(Activity context){
        this.context=context;
    }

    public NewestObservationCacheManager(String cacheDir, Activity context, int maxCacheAmount){
        this.cacheDir=cacheDir;
        this.context=context;
        this.maxCacheAmount=maxCacheAmount;
    }
    public void setCacheDir(String dir){cacheDir=dir;}

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setMaxCacheAmount(int maxCacheAmount) {
        this.maxCacheAmount = maxCacheAmount;
    }

    public void cache(ObservationObject[] observationObjects){
        NewestObservationDBHelper helper=new NewestObservationDBHelper(context);
        SQLiteDatabase readableDatabase=helper.getReadableDatabase();
        SQLiteDatabase writableDatabase=helper.getWritableDatabase();
        String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";

        //Get exist item count
        Cursor cursor= readableDatabase.query(
                ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                null,                                     // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        //Refresh cursor and get existing items count
        int existItemAmount=cursor.getCount();
        cursor= readableDatabase.query(
                ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                null,                                     // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        //Find out how many objects don't exist in DB
        ArrayList<ObservationObject> objectsToInsert=new ArrayList<>();
        for (ObservationObject observationObject:observationObjects) {
            final String whereCause= ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID+"=?";
            final String[] whereArgs={observationObject.nid};
            Cursor c=readableDatabase.query(
                    ObservationContract.NewestObservationEntry.TABLE_NAME,
                    null,
                    whereCause,
                    whereArgs,
                    null,
                    null,
                    null);
            if (c.getCount()==0){
                objectsToInsert.add(observationObject);
            }
        }

        //Insert Objects
        for (int i=0;i<objectsToInsert.size();i++){
            if (existItemAmount>=maxCacheAmount){
                //Delete record in DB and file in cache
                cursor.moveToLast();
                String nid=cursor.getColumnName(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID));
                String localFileUri=cursor.getColumnName(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
                String deleteWhereCause=ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID+"="+nid;
                writableDatabase.delete(ObservationContract.NewestObservationEntry.TABLE_NAME,deleteWhereCause,null);
                File file=new File(localFileUri);
                if (file.exists()){
                    boolean deleted=file.delete();
                    if (!deleted)
                        Log.d("DELETE_FILE_FAILED","File is not deleted");
                }
;            }
            ContentValues values = new ContentValues();
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE, objectsToInsert.get(i).date);
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID, objectsToInsert.get(i).nid);
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, objectsToInsert.get(i).photoLocalUri);
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI, objectsToInsert.get(i).photoServerUri);
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_TITLE, objectsToInsert.get(i).title);
            values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_AUTHOR, objectsToInsert.get(i).author);

            writableDatabase.insert(ObservationContract.NewestObservationEntry.TABLE_NAME,null,values);
        }
    }

    public ArrayList<ObservationObject> getCache(int itemAmount){
        NewestObservationDBHelper dbHelper=new NewestObservationDBHelper(context);
        SQLiteDatabase readableDatabase=dbHelper.getReadableDatabase();
        String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";

        //Get exist item count
        Cursor cursor= readableDatabase.query(
                ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                null,                                     // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder,                                // The sort order
                String.valueOf(itemAmount)                // Limit
        );
        ArrayList<ObservationObject> returnList=new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String emptyStr="";
            String nid=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID));
            String photoLocalUri=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
            String date=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE));
            String title=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_TITLE));
            String photoServerUri=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI));
            String author=cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_AUTHOR));
            returnList.add(new ObservationObject(nid,title,emptyStr,emptyStr,photoServerUri,photoLocalUri,emptyStr,emptyStr,emptyStr,emptyStr,date,author));
            cursor.moveToNext();
        }
        return returnList;
    }
}
