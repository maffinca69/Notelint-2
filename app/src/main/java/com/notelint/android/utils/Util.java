package com.notelint.android.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.notelint.android.Application;

public class Util {
    public static final void showKeyboard() {
        ((InputMethodManager)(Application.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE)))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
