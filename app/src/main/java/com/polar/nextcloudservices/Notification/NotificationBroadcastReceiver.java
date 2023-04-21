package com.polar.nextcloudservices.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polar.nextcloudservices.Services.NotificationService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final NotificationService mService;
    private static final String TAG = "NotificationBroadcastReceiver";
    public NotificationBroadcastReceiver(NotificationService service){
        mService = service;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received notificiation event");
        NotificationEvent event_type = (NotificationEvent) intent.getSerializableExtra("notification_event");
        mService.onNotificationEvent(event_type, intent);
    }
}
