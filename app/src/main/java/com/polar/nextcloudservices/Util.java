package com.polar.nextcloudservices;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.polar.nextcloudservices.Notifications.NotificationUtils;

public class Util {
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //The same as in BasicNotifcationProcessor, but static
    @NonNull
    public static String prettifyChannelName(Context mContext, String Name) {
        String prettyname = NotificationUtils.getTranslation(mContext, Name);
        if(!prettyname.equals(Name)){
            return prettyname;
        }

        String[] parts = Name.split("_");
        StringBuilder nice_name = new StringBuilder();
        for (String part : parts) {
            nice_name.append(part);
        }
        String result = nice_name.toString();
        result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        return result;
    }
}
