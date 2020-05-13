package com.example.sunshine.myruns4.constants;

// TODO: Refactor constants literals into this class making static referencing simpler
public class MyConstants {
    /* PolyLine Constants */
    public static final int POLYLINE_COLOR_PURPLE_ARGB = 0xff81C784;
    ;
    public static final float POLYLINE_STROKE_WIDTH_PX = 12;

    /* Notification Constants */
    public static final CharSequence NOTIFICATION_TITLE = "MyRuns";
    public static final CharSequence NOTIFICATION_CONTENT_TEXT = "Tracking your locations";

    /* Information about Input Type for Various Intents */
    public static final String INPUT_AUTOMATIC = "Automatic";
    public static final String INPUT_GPS = "GPS";
    public static final String INPUT_MANUAL = "Manual";

    /* Tags for getting intent Extras */
    public static final String INPUT_TYPE = "InputType";
    public static final String ACTIVITY_TYPE = "ActivityType";
    public static final String SOURCE = "Source";

    /* Tag for id of Exercise */
    public static final String EXERCISE_ENTRY_ID = "id";
    public static final int FETCH_SINGLE_EXERCISE_ID = 1;


    public static String CURRENT_EXERCISE = "current_exercise";
}
