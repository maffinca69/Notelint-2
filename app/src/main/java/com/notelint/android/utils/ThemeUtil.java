package com.notelint.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.preference.PreferenceManager;

import com.notelint.android.MainActivity;
import com.notelint.android.R;

public class ThemeUtil {
    private static ThemeUtil sInstance;

    private static SharedPreferences mPrefs;

    public static ThemeUtil getInstance(Context c) {
        synchronized (ThemeUtil.class) {
            return sInstance != null ? sInstance : new ThemeUtil(c);
        }
    }

    private ThemeUtil(Context c) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);
        sInstance = this;
    }

    public static int getCurrentTheme() {
        return mPrefs.getInt("current_theme", R.style.AppTheme_Light);
    }

    public static void setCurrentTheme(int themeId) {
        mPrefs.edit().putInt("current_theme", themeId).apply();
    }

    public static void apply(Context c) {
        getInstance(c);
        c.setTheme(getCurrentTheme());
    }

    public static int getCurrentColor(int attr_color) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = MainActivity.getInstance().getTheme();
        theme.resolveAttribute(attr_color, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    public static String getCurrentThemeTitle() {
        switch (getCurrentTheme()) {
            case R.style.AppTheme_Dark:
                return "Темная";
            case R.style.AppTheme_Light:
                return "Светлая";
            default:
                return "Не выбрано";
        }
    }
}
