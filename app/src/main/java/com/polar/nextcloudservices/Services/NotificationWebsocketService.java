package com.polar.nextcloudservices.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONObject;

public class NotificationWebsocketService extends Service implements NotificationListener {
    public NotificationWebsocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onNewNotifications(JSONObject response) {

    }
}