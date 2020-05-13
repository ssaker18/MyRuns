package com.example.sunshine.myruns4.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ExerciseEntry implements Parcelable {
    private Long id;

    private String mInputType;        // Manual, GPS or automatic
    private String mActivityType;     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private String mDuration;         // Exercise duration in seconds
    private String mDistance;      // Distance traveled. Either in meters or feet.
    private String mAvgPace;       // Average pace
    private String mAvgSpeed;      // Average speed
    private String mCalorie;          // Calories burnt
    private String mClimb;         // Climb. Either in meters or feet.
    private String mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private String mTime;
    private String mDate;
    private String mPrivacy;
    private ArrayList<LatLng> mLocationList = new ArrayList<>(); // Location list


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

    protected ExerciseEntry(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        mInputType = in.readString();
        mActivityType = in.readString();
        mDateTime = in.readString();
        mDuration = in.readString();
        mDistance = in.readString();
        mAvgPace = in.readString();
        mAvgSpeed = in.readString();
        mCalorie = in.readString();
        mClimb = in.readString();
        mHeartRate = in.readString();
        mComment = in.readString();
        mTime = in.readString();
        mDate = in.readString();
        mPrivacy = in.readString();
        mLocationList = in.readArrayList(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(mInputType);
        dest.writeString(mActivityType);
        dest.writeString(mDateTime);
        dest.writeString(mDuration);
        dest.writeString(mDistance);
        dest.writeString(mAvgPace);
        dest.writeString(mAvgSpeed);
        dest.writeString(mCalorie);
        dest.writeString(mClimb);
        dest.writeString(mHeartRate);
        dest.writeString(mComment);
        dest.writeString(mTime);
        dest.writeString(mDate);
        dest.writeString(mPrivacy);
        dest.writeArray(mLocationList.toArray());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExerciseEntry> CREATOR = new Creator<ExerciseEntry>() {
        @Override
        public ExerciseEntry createFromParcel(Parcel in) {
            return new ExerciseEntry(in);
        }

        @Override
        public ExerciseEntry[] newArray(int size) {
            return new ExerciseEntry[size];
        }
    };

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

    public ArrayList<LatLng> getLocationList() {
        return this.mLocationList;
    }

    public void setPrivacy(String newPrivacy) {
        this.mPrivacy = newPrivacy;
    }

    public void setLocationList(ArrayList<LatLng> newLocationList) {
        this.mLocationList = newLocationList;
    }

    public String getDate() {
        return this.mDateTime.substring(0, mDateTime.indexOf(" "));
    }

    public String getTime() {
        String time = this.mDateTime.substring(
                mDateTime.indexOf(" ") + 1);

        // May have to strip off seconds component
        if (time.length() > 5) {
            time.substring(0, time.length() - 3);
        }
        return time;

    }
}
