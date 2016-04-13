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
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import DBContract.ObservationContract;
import DBHelper.NewestObservationDBHelper;
import Model.ObservationEntryObject;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationCacheManager {
    static String mCacheDir;
    static Activity mContext;
    final int maxCacheAmount = 300;
    static final Object lock = new Object();

    protected NewestObservationCacheManager() {
    }

    private static class InstanceHolder {
        private static final NewestObservationCacheManager instance = new NewestObservationCacheManager();
    }

    public static NewestObservationCacheManager getInstance(Activity context) {
        mContext = context;
        mCacheDir = context.getCacheDir() + "//Newest_Observation";
        return InstanceHolder.instance;
    }

    public void cache(ObservationEntryObject[] observationEntryObjects) {
        synchronized (lock) {
            NewestObservationDBHelper helper = new NewestObservationDBHelper(mContext);
            SQLiteDatabase readableDatabase = helper.getReadableDatabase();
            SQLiteDatabase writableDatabase = helper.getWritableDatabase();
            String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";

            //Find out how many objects don't exist in DB
            ArrayList<ObservationEntryObject> objectsToInsert = new ArrayList<>();
            for (ObservationEntryObject observationEntryObject : observationEntryObjects) {
                final String whereCause = ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID + "=?";
                final String[] whereArgs = {observationEntryObject.nid};
                Cursor c = readableDatabase.query(
                        ObservationContract.NewestObservationEntry.TABLE_NAME,
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
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE, objectsToInsert.get(i).date);
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID, objectsToInsert.get(i).nid);
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, objectsToInsert.get(i).photoLocalUri);
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI, objectsToInsert.get(i).photoServerUri);
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_TITLE, objectsToInsert.get(i).title);
                values.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_AUTHOR, objectsToInsert.get(i).author);

                writableDatabase.insert(ObservationContract.NewestObservationEntry.TABLE_NAME, null, values);
            }

            //Get all existing items in DB
            Cursor cursor = readableDatabase.query(
                    ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
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
                    String nid = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID));
                    String localFileUri = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
                    String deleteWhereCause = ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID + "=" + nid;
                    writableDatabase.delete(ObservationContract.NewestObservationEntry.TABLE_NAME, deleteWhereCause, null);

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

    public ArrayList<ObservationEntryObject> getCache(int itemAmount) {
        String sortOrder = ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE + " DESC";
        Cursor cursor;
        NewestObservationDBHelper dbHelper = new NewestObservationDBHelper(mContext);
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        synchronized (lock) {
            //Get exist item count
            cursor = readableDatabase.query(
                    ObservationContract.NewestObservationEntry.TABLE_NAME,  // The table to query
                    null,                                     // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder,                                // The sort order
                    String.valueOf(itemAmount)                // Limit
            );
        }
        ArrayList<ObservationEntryObject> returnList = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String emptyStr = "";
            String nid = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID));
            String photoLocalUri = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI));
            String date = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_DATE));
            String title = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_TITLE));
            String photoServerUri = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI));
            String author = cursor.getString(cursor.getColumnIndex(ObservationContract.NewestObservationEntry.COLUMN_NAME_AUTHOR));
            returnList.add(new ObservationEntryObject(nid, title, emptyStr, emptyStr, photoServerUri, photoLocalUri, emptyStr, emptyStr, emptyStr, emptyStr, date, author));
            cursor.moveToNext();
        }
        cursor.close();
        readableDatabase.close();
        return returnList;
    }

    public void clearCache(){
        NewestObservationDBHelper dbHelper = new NewestObservationDBHelper(mContext);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        synchronized (lock){
            writableDatabase.delete(ObservationContract.NewestObservationEntry.TABLE_NAME,null,null);
        }
        writableDatabase.close();
    }
    public int getMaxCacheAmount() {
        return maxCacheAmount;
    }

    public void updateRecordImageLocation(ArrayList<ObservationEntryObject> observationEntryObjects) {
        synchronized (lock) {
            NewestObservationDBHelper dbHelper = new NewestObservationDBHelper(mContext);
            SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
            SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

            for (ObservationEntryObject object : observationEntryObjects) {
                /*First we need to make sure this object still exist in DB
                * Since the object is gotten from Adapter, the corresponding record in DB
                * may be deleted  before we lock the lock object*/
                final String whereCause = ObservationContract.NewestObservationEntry.COLUMN_NAME_NODE_ID + "=?";
                final String[] whereArgs = {object.nid};
                Cursor c = readableDatabase.query(
                        ObservationContract.NewestObservationEntry.TABLE_NAME,
                        null,
                        whereCause,
                        whereArgs,
                        null,
                        null,
                        null);
                if (c.getCount() == 0) {
                    //If the record already deleted, delete the file as well
                    File imgFile = new File(object.photoLocalUri);
                    if (imgFile.exists()) {
                        try {
                            imgFile.delete();
                        } catch (Exception ex) {
                        }
                    }
                    c.close();
                    continue;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(ObservationContract.NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI, object.photoLocalUri);
                writableDatabase.update(ObservationContract.NewestObservationEntry.TABLE_NAME, contentValues, whereCause, whereArgs);
            }
            readableDatabase.close();
            writableDatabase.close();
        }
    }
}
