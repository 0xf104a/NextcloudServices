package com.polar.nextcloudservices;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

public interface AbstractNotificationProcessor {
    public int priority=0;
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context) throws JSONException;
}
