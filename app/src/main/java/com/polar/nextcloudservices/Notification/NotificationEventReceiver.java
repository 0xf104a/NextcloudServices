package com.polar.nextcloudservices.Notification;

import android.content.Intent;

public interface NotificationEventReceiver {
    void onNotificationEvent(NotificationEvent event_type, Intent intent);
}
