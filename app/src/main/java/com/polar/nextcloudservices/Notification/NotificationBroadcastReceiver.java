package com.polar.nextcloudservices.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.polar.nextcloudservices.NotificationService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final NotificationService mService;
    private static final String TAG = "NotificationBroadcastReceiver";
    public NotificationBroadcastReceiver(NotificationService service){
        mService = service;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received notificiation event");
        int event_code = intent.getIntExtra("notification_event", 0);
        mService.onNotificationEvent(NotificationEvent.fromInt(event_code), intent);
    }
}
