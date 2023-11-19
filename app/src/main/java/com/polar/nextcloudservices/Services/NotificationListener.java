package com.polar.nextcloudservices.Services;

import org.json.JSONObject;

public interface NotificationListener {
    void onNewNotifications(JSONObject response);
}
