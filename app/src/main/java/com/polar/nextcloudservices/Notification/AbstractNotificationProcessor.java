package com.polar.nextcloudservices.Notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

public interface AbstractNotificationProcessor {
    /**
     * @param id Notification ID in Nextcloud
     * @param builderResult a notification builder result which should be updated
     * @param manager An Android notification manager service
     * @param rawNotification JSON representation of notification
     * @param context Android Context object for interacting with Android
     * @param controller object which is used for coordinating all notification activity
     * @return a result of builder
     * @throws Exception throws any exception which occured in notification processors
     */
    NotificationBuilderResult updateNotification(int id, NotificationBuilderResult builderResult,
                                                  NotificationManager manager,
                                                  JSONObject rawNotification,
                                                  Context context, NotificationController controller) throws Exception;

    /**
     * @param event event that occured
     * @param intent intent triggered by event
     * @param controller object which is used for coordinating all notification activity
     */
    void onNotificationEvent(NotificationEvent event, Intent intent,
                             NotificationController controller);

    /**
     * @return Priority of notification processor
     */
    int getPriority();
}
