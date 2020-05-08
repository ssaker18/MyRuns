package com.example.sunshine.myruns3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.sunshine.myruns3.LoginActivity;
import com.example.sunshine.myruns3.R;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        Preference signOut = getPreferenceScreen().findPreference("sign_out");
        if (signOut != null) {
            signOut.setOnPreferenceClickListener(this);
        }
    }

    /*
     * Signs out the User if the signOut preference is clicked
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("sign_out")) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.finish();
                return true;
            }
        }
        return false;
    }
}
