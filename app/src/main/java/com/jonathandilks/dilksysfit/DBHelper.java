package com.jonathandilks.dilksysfit;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;


/**
 * Created by jonathan on 27/11/17.
 */

class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "runsDB", null, 7);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE point_data " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "run_id INTEGER, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "lat FLOAT(10, 6) NOT NULL , " +
                "lng FLOAT(10, 6) NOT NULL , " +
                "alt DOUBLE," +
                "speed FLOAT" +
                ");");

        db.execSQL("CREATE TABLE run_summaries " +
                "(_id INTEGER PRIMARY KEY, " +
                "name TEXT," +
                "finish_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "finish_location_name TEXT," +
                "total_time INTEGER," +
                "total_distance FLOAT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //No upgrade version needed yet
    }
}
