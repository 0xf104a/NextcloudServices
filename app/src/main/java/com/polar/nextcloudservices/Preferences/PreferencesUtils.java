package com.polar.nextcloudservices.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

public class PreferencesUtils {

    public static String NONE_RESULT = "<none>";

    public static String getPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, NONE_RESULT);
    }

    public static Integer getIntPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, Integer.MIN_VALUE);
    }

    public static boolean getBoolPreference(Context context, String key, boolean fallback) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, fallback);
    }

    public static boolean isPreferenceSet(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains(key);
    }

    public static void setSSOPreferences(Context context, @NonNull SingleSignOnAccount account) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("sso_enabled", true);
        editor.putString("sso_name", account.name);
        editor.putString("sso_server", account.url);
        editor.putString("sso_type", account.type);
        editor.putString("sso_token", account.token);
        editor.putString("sso_userid", account.userId);
        editor.apply();
    }

    public static void setStringPreference(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setBoolPreference(Context context, String key, Boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
