package com.example.echo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
}