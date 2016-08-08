package edu.wit.wongh2.duonge1.blindeye;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by pizza on 8/7/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}