package com.polar.nextcloudservices.Notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Services.NotificationService;

import org.json.JSONObject;

public interface AbstractNotificationProcessor {
    NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context, NotificationController controller) throws Exception;

    void onNotificationEvent(NotificationEvent event, Intent intent,
                             NotificationController controller);

    int getPriority();
}
