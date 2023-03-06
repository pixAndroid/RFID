package com.example.uhf_bt.ftp_client;


import android.content.Context;
import android.content.SharedPreferences;

/********************************************
 *     Created by DailyCoding on 18-Jan-23.  *
 ********************************************/

public class FtpPref {

    private static final String PREF_FTP_FILE = "PREF_FTP_FILE_SETTINGS";
    private static final String KEY_ADDRESS = "KEY_ADDRESS";
    private static final String KEY_PORT = "KEY_PORT";
    private static final String KEY_USERNAME = "KEY_USERNAME";
    private static final String KEY_PASSWORD = "KEY_PASSWORD";

    public static void saveFtpAddress(Context context, String address) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_ADDRESS, address).apply();
    }

    public static void saveFtpPort(Context context, String port) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PORT, port).apply();
    }

    public static void saveFtpUsername(Context context, String username) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public static void saveFtpPassword(Context context, String password) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PASSWORD, password).apply();
    }

    //READ
    public static String readFtpAddress(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        return preferences.getString(KEY_ADDRESS, "");
    }

    public static String readFtpPort(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PORT, "");
    }

    public static String readFtpUsername(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USERNAME, "");
    }

    public static String readFtpPassword(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FTP_FILE, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PASSWORD, "");
    }


}
