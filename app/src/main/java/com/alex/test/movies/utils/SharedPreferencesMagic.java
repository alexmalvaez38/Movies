package com.alex.test.movies.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesMagic {

    public static void setString(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, Constants.DEFAULT_SHARED_VALUE);
    }

}
