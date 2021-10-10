package com.polar.nextcloudservices.NotificationProcessors;

import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

public interface AbstractNotificationProcessor {
    public int priority=0;
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification) throws JSONException;
}
