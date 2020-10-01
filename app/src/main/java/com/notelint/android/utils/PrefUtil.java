package com.notelint.android.utils;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.notelint.android.Application;

import java.util.Objects;

public class PrefUtil {
    private static SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());

    public static final int getNotifyTimeRepeater() {
        return Integer.parseInt(Objects.requireNonNull(mPrefs.getString("notify_repeater", "0")));
    }
}