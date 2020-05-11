package com.example.sunshine.myruns4.models;

public class ExerciseEntry {
    private Long id;

    private String mInputType;        // Manual, GPS or automatic
    private String mActivityType;     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private String mDuration;         // Exercise duration in seconds
    private String mDistance;      // Distance traveled. Either in meters or feet.
    private String mAvgPace;       // Average pace
    private String mAvgSpeed;      // Average speed
    private String mCalorie ;          // Calories burnt
    private String mClimb;         // Climb. Either in meters or feet.
    private String mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private String mTime;
    private String mDate;
    private String mPrivacy;
    private  String mGPS;

//     private ArrayList<Lat,Lng> mLocationList; // Location list


    /*
      Initialises a new Exercise Entry with id
     */
    public ExerciseEntry(Long id) {
        this.id = id;
    }

    /*
     * Initialises empty new Exercise Entry
     */
    public ExerciseEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInputType() {
        return mInputType;
    }

    public void setInputType(String mInputType) {
        this.mInputType = mInputType;
    }

    public String getActivityType() {
        return mActivityType;
    }

    public void setActivityType(String mActivityType) {
        this.mActivityType = mActivityType;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public void setDateTime(String dateTime) {
        this.mDateTime = dateTime;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String mDistance) {
        this.mDistance = mDistance;
    }

    public String getAvgPace() {
        return mAvgPace;
    }

    public void setAvgPace(String mAvgPace) {
        this.mAvgPace = mAvgPace;
    }

    public String getAvgSpeed() {
        return mAvgSpeed;
    }

    public void setAvgSpeed(String mAvgSpeed) {
        this.mAvgSpeed = mAvgSpeed;
    }

    public String getCalorie() {
        return mCalorie;
    }

    public void setCalorie(String mCalorie) {
        this.mCalorie = mCalorie;
    }

    public String getClimb() {
        return mClimb;
    }

    public void setClimb(String mClimb) {
        this.mClimb = mClimb;
    }

    public String getHeartRate() {
        return mHeartRate;
    }

    public void setHeartRate(String mHeartRate) {
        this.mHeartRate = mHeartRate;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }


    public void setTime(String time) {
        this.mTime = time;
        setDateTime(mDate + " " + time);
    }

    public void setDate(String date) {
        this.mDate = date;
        setDateTime(date + " " + mTime);
    }

    public String getPrivacy() {
        return this.mPrivacy;
    }

    public String getGPS() {
        return this.mGPS;
    }

    public void setPrivacy(String newPrivacy){
        this.mPrivacy = newPrivacy;
    }

    public void setGPS(String newGPS){
        this.mGPS = newGPS;
    }

    public String getDate() {
        return this.mDateTime.substring(0, mDateTime.indexOf(" "));
    }
    public String getTime() {
        return this.mDateTime.substring(mDateTime.indexOf(" ") + 1);
    }
}
