package com.polar.nextcloudservices.Notifications;

import android.content.Context;

import com.polar.nextcloudservices.R;

public class NotificationUtils {
    public static String getTranslation(Context context, String app){
        switch (app){
            case "spreed": return context.getString(R.string.applist_talk);
            case "dav": return context.getString(R.string.applist_dav);
            case "updatenotification": return context.getString(R.string.applist_updates);
            default: return app;
        }
    }
}
