package com.polar.nextcloudservices.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.API.websocket.NotificationWebsocket;
import com.polar.nextcloudservices.API.websocket.NotificationWebsocketEventListener;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.StatusController;
import com.polar.nextcloudservices.Utils.CommonUtil;

import org.json.JSONObject;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class NotificationWebsocketService extends Service implements NotificationWebsocketEventListener {
    private NextcloudAbstractAPI mAPI;
    private ServiceSettings mServiceSettings;
    private NotificationController mNotificationController;
    private ConnectionController mConnectionController;
    private StatusController mStatusController;
    private NotificationWebsocket mNotificationWebsocket;
    private final static String TAG = "Services.NotificationWebsocketService";

    @Override
    public void onCreate(){
        mServiceSettings = new ServiceSettings(this);
        mAPI = mServiceSettings.getAPIFromSettings();
        mNotificationController = new NotificationController(this, mServiceSettings);
        mStatusController = new StatusController(this);
        mConnectionController = new ConnectionController(mServiceSettings);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mNotificationController,
                NotificationServiceConfig.NOTIFICATION_CONTROLLER_PRIORITY);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mConnectionController, NotificationServiceConfig.CONNECTION_COMPONENT_PRIORITY);
        startListening();
        startForeground(1, mNotificationController.getServiceNotification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onNewNotifications(JSONObject response) {
        if(response != null){
            mNotificationController.onNotificationsUpdated(response);
        }else{
            Log.e(TAG, "null response for notifications");
        }
    }

    private void startListening(){
        try {
            mNotificationWebsocket = mAPI.getNotificationsWebsocket(this);
            mStatusController.addComponent(
                    NotificationServiceComponents.SERVICE_COMPONENT_WEBSOCKET,
                    mNotificationWebsocket, NotificationServiceConfig.API_COMPONENT_PRIORITY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //Initially get notifications
        mAPI.getNotifications(this);
    }

    /**
     * @param isError whether disconnect resulted from error
     */
    @Override
    public void onWebsocketDisconnected(boolean isError) {
        if(mConnectionController.checkConnection(this)){
            Log.w(TAG, "Received disconnect from websocket. Restart pause 3 seconds");
            CommonUtil.safeSleep(3000);
            startListening();
        } else {
            Log.w(TAG, "Disconnected from websocket. Seems that we have no network");
        }
    }

    @Override
    public void onWebsocketConnected() {
        /* stub */
    }
}