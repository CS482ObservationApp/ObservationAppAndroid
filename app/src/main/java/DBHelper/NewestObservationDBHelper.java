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

package DBHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import DBContract.ObservationContract;
import DBContract.ObservationContract.NewestObservationEntry;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ObservationContract.NewestObservationEntry.TABLE_NAME + " (" +
                    NewestObservationEntry._ID + " INTEGER PRIMARY KEY," +
                    NewestObservationEntry.COLUMN_NAME_NODE_ID + INTEGER_TYPE + COMMA_SEP +
                    NewestObservationEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NewestObservationEntry.COLUMN_NAME_PHOTO_SERVER_URI + TEXT_TYPE + COMMA_SEP +
                    NewestObservationEntry.COLUMN_NAME_PHOTO_LOCAL_URI + TEXT_TYPE + COMMA_SEP +
                    NewestObservationEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE +COMMA_SEP+
                    NewestObservationEntry.COLUMN_NAME_DATE+ TEXT_TYPE +
            " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ObservationContract.NewestObservationEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NewestObservation.db";



    public NewestObservationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
