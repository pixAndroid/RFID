package com.example.uhf_bt.ftp_client;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/********************************************
 *     Created by DailyCoding on 30-Dec-22.  *
 ********************************************/

public class TimeUtil {
    private static final String TAG = "TimeUtil";

    public static String convertLongTimeToDate(long timeInMillis) {
        Date d = new Date(Long.valueOf(timeInMillis));
        SimpleDateFormat formatter;
        //formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z"); // complete formatted
        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // complete formatted
        String strDate = formatter.format(d);
        Log.d(TAG, strDate);
        return strDate;
    }
}
