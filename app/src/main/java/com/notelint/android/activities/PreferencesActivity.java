package com.notelint.android.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.preference.PreferenceFragmentCompat;

import com.notelint.android.R;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
