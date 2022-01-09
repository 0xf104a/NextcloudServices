package com.polar.nextcloudservices.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppPreferences {

    private static String PREFERENCE_PREFIX = "app_state_";
    private static String PREFERENCE_LIST = "app_statelist";

    public static boolean isAppEnabled(Context context, String key) {
        if(!PreferencesUtils.isPreferenceSet(context, PREFERENCE_PREFIX+key)){
            addApplist(context, key);
            PreferencesUtils.setBoolPreference(context, PREFERENCE_PREFIX+key, true);
        }
        return PreferencesUtils.getBoolPreference(context, PREFERENCE_PREFIX+key, true);
    }

    public static void setEnabled(Context context, String key, Boolean value) {
        PreferencesUtils.setBoolPreference(context, PREFERENCE_PREFIX+key, value);
    }

    public static ArrayList<String> getApplist(Context context){
        String content = PreferencesUtils.getPreference(context, PREFERENCE_LIST);
        ArrayList<String> results = new ArrayList();
        try {
            JSONArray array = new JSONArray(content);
            for(int i = 0; i < array.length(); i++) {
                if(!results.contains(array.getString(i))){
                    results.add(array.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void addApplist(Context context, String value) {
        JSONArray array = new JSONArray();
        for (String s: getApplist(context)) {
            array.put(s);
        }
        if(!getApplist(context).contains(value)){
            array.put(value);
        }
        PreferencesUtils.setStringPreference(context, PREFERENCE_LIST, array.toString());
    }

}
