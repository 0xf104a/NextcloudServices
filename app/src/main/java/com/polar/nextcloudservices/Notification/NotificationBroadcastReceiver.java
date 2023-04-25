package com.polar.nextcloudservices.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final NotificationEventReceiver mNotificationEventReceiver;
    private static final String TAG = "Notification.NotificationBroadcastReceiver";
    public NotificationBroadcastReceiver(NotificationEventReceiver notificationEventReceiver){
        mNotificationEventReceiver = notificationEventReceiver;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received notification event");
        NotificationEvent event_type = (NotificationEvent) intent.getSerializableExtra("notification_event");
        mNotificationEventReceiver.onNotificationEvent(event_type, intent);
    }
}
