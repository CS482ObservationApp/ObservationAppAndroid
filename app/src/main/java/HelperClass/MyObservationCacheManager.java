package HelperClass;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import DBContract.ObservationContract;
import DBHelper.MyObservationDBHelper;
import DBHelper.NewestObservationDBHelper;
import Model.ObservationEntryObject;

/**
 * Created by zhuol on 3/21/2016.
 */
public class MyObservationCacheManager {
    static  String mCacheDir;
    static Activity mContext;
    final int maxCacheAmount=5000;
    static final Object lock=new Object();

    protected MyObservationCacheManager(){}
    private static class InstanceHolder {
        private static final MyObservationCacheManager instance = new MyObservationCacheManager();
    }

    public static MyObservationCacheManager getInstance(Activity context){
        mContext =context;
        mCacheDir = context.getCacheDir() + "//My_Observation";
        return InstanceHolder.instance;
    }

    public void cache(ObservationEntryObject[] observationEntryObjects){
        MyObservationDBHelper helper=new MyObservationDBHelper(mContext);
        SQLiteDatabase readableDatabase=helper.getReadableDatabase();
        SQLiteDatabase writableDatabase=helper.getWritableDatabase();
        String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";

        synchronized(lock) {
            //Find out how many objects don't exist in DB
            ArrayList<ObservationEntryObject> objectsToInsert = new ArrayList<>();
            for (ObservationEntryObject observationEntryObject : observationEntryObjects) {
                final String whereCause = ObservationContract.MyObservationEntry.COLUMN_NAME_NODE_ID + "=?";
                final String[] whereArgs = {observationEntryObject.nid};
                Cursor c = readableDatabase.query(
                        ObservationContract.MyObservationEntry.TABLE_NAME,
                        null,
                        whereCause,
                        whereArgs,
                        null,
                        null,
                        null);
                if (c.getCount() == 0) {
                    objectsToInsert.add(observationEntryObject);
                }
                c.close();
            }

            //Insert Objects
            for (int i = 0; i < objectsToInsert.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(ObservationContract.MyObservationEntry.COLUMN_NAME_DATE, objectsToInsert.get(i).date);
                values.put(ObservationContract.MyObservationEntry.COLUMN_NAME_NODE_ID, objectsToInsert.get(i).nid);
                values.put(ObservationContract.MyObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, objectsToInsert.get(i).photoLocalUri);
                values.put(ObservationContract.MyObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI, objectsToInsert.get(i).photoServerUri);
                values.put(ObservationContract.MyObservationEntry.COLUMN_NAME_TITLE, objectsToInsert.get(i).title);

                writableDatabase.insert(ObservationContract.MyObservationEntry.TABLE_NAME, null, values);
            }

            //Get all existing items in DB
            Cursor cursor = readableDatabase.query(
                    ObservationContract.MyObservationEntry.TABLE_NAME,  // The table to query
                    null,                                     // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
            int existItemAmount = cursor.getCount();

            //Delete the oldest item if the items is more than max
            if (existItemAmount >= maxCacheAmount) {
                cursor.moveToLast();
                for (int i = 0; i < existItemAmount - maxCacheAmount; i++) {
                    //Delete record in DB and file in cache
                    String nid = cursor.getColumnName(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_NODE_ID));
                    String localFileUri = cursor.getColumnName(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
                    String deleteWhereCause = ObservationContract.MyObservationEntry.COLUMN_NAME_NODE_ID + "=" + nid;
                    writableDatabase.delete(ObservationContract.MyObservationEntry.TABLE_NAME, deleteWhereCause, null);

                    File file = new File(localFileUri);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (!deleted)
                            Log.d("DELETE_FILE_FAILED", "File is not deleted");
                    }
                    cursor.moveToPrevious();
                }
            }
            cursor.close();
            writableDatabase.close();
            readableDatabase.close();
        }
    }
    public ArrayList<ObservationEntryObject> getCache(int itemAmount){
        MyObservationDBHelper dbHelper=new MyObservationDBHelper(mContext);
        SQLiteDatabase readableDatabase=dbHelper.getReadableDatabase();
        String sortOrder = ObservationContract.MyObservationEntry.COLUMN_NAME_DATE + " DESC";
        Cursor cursor;
        synchronized(lock) {
            //Get exist item count
            cursor = readableDatabase.query(
                    ObservationContract.MyObservationEntry.TABLE_NAME,  // The table to query
                    null,                                     // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder,                                // The sort order
                    String.valueOf(itemAmount)                // Limit
            );
        }
        ArrayList<ObservationEntryObject> returnList=new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            final String EMPTY_STR="";
            String nid=cursor.getString(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_NODE_ID));
            String photoLocalUri=cursor.getString(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
            String date=cursor.getString(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_DATE));
            String title=cursor.getString(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_TITLE));
            String photoServerUri=cursor.getString(cursor.getColumnIndex(ObservationContract.MyObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI));
            returnList.add(new ObservationEntryObject(nid,title,EMPTY_STR,EMPTY_STR,photoServerUri,photoLocalUri,EMPTY_STR,EMPTY_STR,EMPTY_STR,EMPTY_STR,date,EMPTY_STR));
            cursor.moveToNext();
        }
        cursor.close();
        readableDatabase.close();
        return returnList;
    }
    public int getMaxCacheAmount(){
        return maxCacheAmount;
    }
}
