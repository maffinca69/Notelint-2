package com.notelint.android.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String FORMAT = "dd.MM.yyyy H:mm";

    public static final String getFormattedDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);
        return formatter.format(new Date(Long.parseLong(String.valueOf(timestamp))));
    }

}
