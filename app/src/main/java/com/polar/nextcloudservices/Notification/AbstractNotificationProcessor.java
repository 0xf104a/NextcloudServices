package com.polar.nextcloudservices.Notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public interface AbstractNotificationProcessor {
    NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context, NotificationService service) throws Exception;

    void onNotificationEvent(NotificationEvent event, Intent intent, NotificationService service);


    int getPriority();
}
