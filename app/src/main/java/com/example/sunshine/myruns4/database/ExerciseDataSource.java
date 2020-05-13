package com.example.sunshine.myruns4.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sunshine.myruns4.models.ExerciseEntry;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDataSource {
    private static final String TAG =  ExerciseDataSource.class.getName();

    // Database fields
    private SQLiteDatabase db;
    private ExerciseEntryDbHelper dbHelper;
    private String[] allColumns = {
            ExerciseEntryDbHelper.COLUMN_ID, ExerciseEntryDbHelper.COLUMN_DATA_TIME,
            ExerciseEntryDbHelper.COLUMN_COMMENT,  ExerciseEntryDbHelper.COLUMN_DISTANCE,
            ExerciseEntryDbHelper.COLUMN_AVG_PACE, ExerciseEntryDbHelper.COLUMN_DURATION,
            ExerciseEntryDbHelper.COLUMN_AVG_SPEED, ExerciseEntryDbHelper.COLUMN_CLIMB,
            ExerciseEntryDbHelper.COLUMN_HEARTRATE, ExerciseEntryDbHelper.COLUMN_CALORIES,
            ExerciseEntryDbHelper.COLUMN_PRIVACY, ExerciseEntryDbHelper.COLUMN_GPS_DATA,
            ExerciseEntryDbHelper.COLUMN_ACTIVITY_TYPE, ExerciseEntryDbHelper.COLUMN_INPUT_TYPE,
             };

    public ExerciseDataSource(Context context){
        dbHelper = new ExerciseEntryDbHelper(context);
    }

    /*
     * opens the database for writing and reading exercises
     */
    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    /*
     * closes the database, after which no read/write can be done
     */
    public void close() {
        dbHelper.close();
    }


    /*
     * Inserts an Exercise entry given each column value
     */
    public long insertEntry(ExerciseEntry entry) {
        ContentValues values = new ContentValues();
        // fill in content values with respective exercise header info
        fillContentValues(values, entry);

        long insertId = db.insert(ExerciseEntryDbHelper.EXERCISES_TABLE, null,
                values);

        entry.setId(insertId);

        return insertId;
    }


    /*
     * Queries a specific entry by its index.
     */
    public ExerciseEntry fetchEntryByIndex(long rowId) {
        Cursor cursor = db.query(ExerciseEntryDbHelper.EXERCISES_TABLE,
                allColumns, ExerciseEntryDbHelper.COLUMN_ID + " = " + rowId + ";",
                null, null, null, null);
        cursor.moveToFirst();
        ExerciseEntry fetchedEntry =  exerciseFromCursor(cursor);
        cursor.close();
        return  fetchedEntry;
    }

    /*
     * Queries the entire table for exercise entries, returns all rows
     * as List of ExerciseEntry Objects
     */
    public ArrayList<ExerciseEntry> fetchEntries() {
        ArrayList<ExerciseEntry> exercises = new ArrayList<>();

        Cursor cursor = db.query(ExerciseEntryDbHelper.EXERCISES_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ExerciseEntry exerciseEntry = exerciseFromCursor(cursor);
            Log.d(TAG, "get comment = " + exerciseFromCursor(cursor).toString());
            exercises.add(exerciseEntry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return exercises;
    }

    /*
     * Converts a cursor to an exercise object
     * Returns null on empty cursor
     */
    private ExerciseEntry exerciseFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        int INDEX_ID = cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_ID);
        int INDEX_ACTIVITY_TYPE =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_ACTIVITY_TYPE);
        int INDEX_INPUT_TYPE =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_INPUT_TYPE);
        int INDEX_DATA_TIME =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_DATA_TIME);
        int INDEX_DISTANCE =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_DISTANCE);
        int INDEX_DURATION =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_DURATION);
        int INDEX_HEARTRATE =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_HEARTRATE);
        int INDEX_COMMENT =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_COMMENT);
        int INDEX_AVG_PACE =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_AVG_PACE);
        int INDEX_AVG_SPEED =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_AVG_SPEED);
        int INDEX_CALORIES =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_CALORIES);
        int INDEX_CLIMB =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_CLIMB );
        int INDEX_GPS =  cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_GPS_DATA);
        int INDEX_PRIVACY = cursor.getColumnIndex(ExerciseEntryDbHelper.COLUMN_PRIVACY);

        ExerciseEntry exercise = new ExerciseEntry(cursor.getLong(INDEX_ID));

        exercise.setInputType(cursor.getString(INDEX_INPUT_TYPE));
        exercise.setActivityType(cursor.getString(INDEX_ACTIVITY_TYPE));
        exercise.setDateTime(cursor.getString(INDEX_DATA_TIME));
        exercise.setDuration(cursor.getString(INDEX_DURATION));
        exercise.setDistance(cursor.getString(INDEX_DISTANCE));
        exercise.setCalorie(cursor.getString(INDEX_CALORIES));
        exercise.setHeartRate(cursor.getString(INDEX_HEARTRATE));
        exercise.setComment(cursor.getString(INDEX_COMMENT));
        exercise.setAvgPace(cursor.getString(INDEX_AVG_PACE));
        exercise.setAvgSpeed(cursor.getString(INDEX_AVG_SPEED));
        exercise.setClimb(cursor.getString(INDEX_CLIMB));
        exercise.setPrivacy(cursor.getString(INDEX_PRIVACY));
        exercise.setLocationList(JsonToLocations(cursor.getString(INDEX_GPS)));

        return exercise;
    }

    /*
     * Converts back JSON to an ArrayList of LatLngs
     */
    private ArrayList<LatLng> JsonToLocations(String string){

        Log.d(TAG, "JsonToLocations() received " + string);
        JSONArray jsonArray = null;
        ArrayList<LatLng> locations = new ArrayList<>();
        try {
            jsonArray = new JSONArray(string);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                // TODO: create new LatLng each time
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                System.out.println(jsonObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }

    /*
     * Helper method used for insert: populates Content Values object
     */
    private void fillContentValues(ContentValues values, ExerciseEntry entry) {
        values.put(ExerciseEntryDbHelper.COLUMN_ACTIVITY_TYPE, entry.getActivityType());
        values.put(ExerciseEntryDbHelper.COLUMN_INPUT_TYPE, entry.getInputType());
        values.put(ExerciseEntryDbHelper.COLUMN_DATA_TIME, entry.getDateTime());
        values.put(ExerciseEntryDbHelper.COLUMN_DURATION, entry.getDuration());
        values.put(ExerciseEntryDbHelper.COLUMN_DISTANCE, entry.getDistance());
        values.put(ExerciseEntryDbHelper.COLUMN_CALORIES, entry.getCalorie());
        values.put(ExerciseEntryDbHelper.COLUMN_HEARTRATE, entry.getHeartRate());
        values.put(ExerciseEntryDbHelper.COLUMN_COMMENT, entry.getComment());
        values.put(ExerciseEntryDbHelper.COLUMN_CLIMB, entry.getClimb());
        values.put(ExerciseEntryDbHelper.COLUMN_AVG_PACE, entry.getAvgPace());
        values.put(ExerciseEntryDbHelper.COLUMN_AVG_SPEED, entry.getAvgSpeed());
        values.put(ExerciseEntryDbHelper.COLUMN_GPS_DATA, locationListToJSON(entry.getLocationList()));
        values.put(ExerciseEntryDbHelper.COLUMN_PRIVACY, entry.getPrivacy());
    }

    /*
     * Converts a location ArrayList to JSON in order to simplify
     * insertion into the database
     */
    private String locationListToJSON(ArrayList<LatLng> locationList){
        List<LatLng> list = locationList;
        JSONArray jsArray = new JSONArray(list);
//        for (int i = 0; i < locationList.size(); i++) {
//            jsArray.put(locationList.get(i).latitude + " " + locationList.get(i).longitude);
//        }
        Log.d(TAG, "convertToJSONArray() " + jsArray.toString());
        return jsArray.toString();
    }

    /*
     * Remove an entry by giving its index
     */
    public void deleteEntryByIndex(Long[] id) {
        db.delete(ExerciseEntryDbHelper.EXERCISES_TABLE, ExerciseEntryDbHelper.COLUMN_ID
                + " = " + id[0], null);
        Log.d(TAG, "deleted Exercise with _ID = " + id[0]);
    }
}
