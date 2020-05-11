package com.example.sunshine.myruns4.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class ExerciseEntryDbHelper extends SQLiteOpenHelper {

    private static final String TAG = ExerciseEntryDbHelper.class.getName();

    /*
     * Database Name and Version
     */
    private static final String DATABASE_NAME = "exercises.db";
    private static final int DATABASE_VERSION = 1;

    /*
     * Database column Headers:
     */
    static final String COLUMN_INPUT_TYPE = "input_type";
    static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    static final String COLUMN_DATA_TIME = "data_time";
    static final String COLUMN_DURATION = "duration";
    static final String COLUMN_DISTANCE = "distance";
    static final String COLUMN_AVG_PACE = "avg_pace";
    static final String COLUMN_AVG_SPEED = "avg_speed";
    static final String COLUMN_CALORIES = "calories";
    static final String COLUMN_CLIMB = "climb";
    static final String COLUMN_HEARTRATE = "heartrate";
    static final String COLUMN_COMMENT = "comment";
    static final String COLUMN_PRIVACY = "privacy";
    static final String COLUMN_GPS_DATA = "gps_data";
    static final String COLUMN_ID = "_id";

    /*
     * Single Table in Database: exercises_table
     */
    static final String EXERCISES_TABLE = "exercises_table";


    // Database creation sql statement
    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + EXERCISES_TABLE + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_INPUT_TYPE + " TEXT NOT NULL,"
            + COLUMN_ACTIVITY_TYPE + " TEXT NOT NULL,"
            + COLUMN_DATA_TIME + " TEXT NOT NULL,"
            + COLUMN_DURATION + " TEXT NOT NULL,"
            + COLUMN_DISTANCE + " TEXT,"
            + COLUMN_AVG_PACE + " TEXT,"
            + COLUMN_AVG_SPEED + " TEXT,"
            + COLUMN_CALORIES + " TEXT,"
            + COLUMN_CLIMB + " TEXT,"
            + COLUMN_HEARTRATE + " TEXT,"
            + COLUMN_COMMENT + " TEXT,"
            + COLUMN_PRIVACY + " TEXT,"
            + COLUMN_GPS_DATA + " TEXT);";

    // Database updating sql statement
    private static final String DATABASE_UPGRADE = "DROP TABLE IF EXISTS " + EXERCISES_TABLE;


    ExerciseEntryDbHelper(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Created Exercises Database");
        db.execSQL(DATABASE_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Updating Exercises Database from version "
                + oldVersion + " to " + newVersion);
        db.execSQL(DATABASE_UPGRADE);
        onCreate(db);
    }
}
