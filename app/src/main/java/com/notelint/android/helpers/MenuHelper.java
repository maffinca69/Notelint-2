package com.notelint.android.helpers;

import android.graphics.PorterDuff;
import android.view.Menu;

import com.notelint.android.R;
import com.notelint.android.utils.ThemeUtil;

public class MenuHelper {
    public static void setColorIcons(Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).getIcon().mutate().setColorFilter(ThemeUtil.getCurrentColor(R.attr.text_primary), PorterDuff.Mode.SRC_IN);
        }
    }
}
