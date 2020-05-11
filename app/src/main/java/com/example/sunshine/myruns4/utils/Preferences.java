package com.example.sunshine.myruns4.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.sunshine.myruns4.R;


public class Preferences {
    protected SharedPreferences mSharedPreference;

    public Preferences(Context context) {
        this.mSharedPreference = context.getSharedPreferences(context.
                getString(R.string.shared_preference_key), context.MODE_PRIVATE);
    }

    public String getName() {
        return mSharedPreference.getString("name","nan");
    }

    public void setName(String name) {
        mSharedPreference.edit().putString("name", name).apply();;

    }

    public String getEmail() {
        return mSharedPreference.getString("email","nan");
    }

    public void setEmail(String email) {
        mSharedPreference.edit().putString("email", email).apply();

    }

    public String getMajor() {
        return mSharedPreference.getString("major","nan");
    }

    public void setMajor(String major) {
        mSharedPreference.edit().putString("major", major).apply();
    }

    public String getYearGroup() {
        return mSharedPreference.getString("yearGroup","nan");
    }

    public void setYearGroup(String yearGroup) {
        mSharedPreference.edit().putString("yearGroup", yearGroup).apply();
    }

    public String getPassWord() {
        return mSharedPreference.getString("passWord","nan");
    }

    public void setPassWord(String passWord) {
        mSharedPreference.edit().putString("passWord", passWord).apply();
    }

    public String getPhone() {
        return mSharedPreference.getString("phone","nan");
    }

    public void setPhone(String mPhone) {
        mSharedPreference.edit().putString("phone", mPhone).apply();;
    }

    public int getGender() {
        return mSharedPreference.getInt("gender", 99);
    }

    public void setGender(int mGender) {
        mSharedPreference.edit().putInt("gender", mGender).apply();;
    }

    public void setImagePath(String imagePath){
        mSharedPreference.edit().putString("picture", imagePath).apply();
    }

    public String getImagePath(){
       return mSharedPreference.getString("picture", "nan");
    }

    public void clearProfile(){
        mSharedPreference.edit().remove("name").apply();
        mSharedPreference.edit().remove("email").apply();
        mSharedPreference.edit().remove("passWord").apply();
        mSharedPreference.edit().remove("gender").apply();
        mSharedPreference.edit().remove("phone").apply();
        mSharedPreference.edit().remove("major").apply();
        mSharedPreference.edit().remove("class").apply();
        mSharedPreference.edit().remove("picture").apply();
    }

    public SharedPreferences getSharedPreference() {
        return mSharedPreference;
    }

    @NonNull
    @Override
    public String toString() {
        return mSharedPreference.getString("name", "nan") + " " +
                mSharedPreference.getString("email", "nan") + " " +
                mSharedPreference.getString("passWord", "nan") + " " +
                mSharedPreference.getString("phone", "nan") + " " +
                mSharedPreference.getString("major", "nan") + " " +
                mSharedPreference.getString("yearGroup", "nan") + " ";
    }
}
