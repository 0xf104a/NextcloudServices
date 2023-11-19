package com.polar.nextcloudservices.Services;

import org.json.JSONObject;

/**
 * An interface for delivering new notifications
 */
public interface NotificationListener {
    void onNewNotifications(JSONObject response);
}
