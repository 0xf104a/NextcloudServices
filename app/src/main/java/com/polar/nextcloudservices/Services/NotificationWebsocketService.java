package com.polar.nextcloudservices.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.polar.nextcloudservices.API.INextcloudAbstractAPI;
import com.polar.nextcloudservices.API.websocket.NotificationWebsocket;
import com.polar.nextcloudservices.API.websocket.INotificationWebsocketEventListener;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.StatusController;
import com.polar.nextcloudservices.Utils.CommonUtil;

import org.json.JSONObject;

public class NotificationWebsocketService extends Service
        implements INotificationWebsocketEventListener, 
        IConnectionStatusListener, INotificationService {
    private INextcloudAbstractAPI mAPI;
    private ServiceSettings mServiceSettings;
    private NotificationController mNotificationController;
    private ConnectionController mConnectionController;
    private StatusController mStatusController;
    private NotificationWebsocket mNotificationWebsocket;
    private NotificationServiceBinder mBinder;
    private final static String TAG = "Services.NotificationWebsocketService";

    @Override
    public void onCreate(){
        mBinder = new NotificationServiceBinder(this);
        mNotificationController = new NotificationController(this, mServiceSettings);
        startForeground(1, mNotificationController.getServiceNotification());
        mServiceSettings = new ServiceSettings(this);
        mAPI = mServiceSettings.getAPIFromSettings();
        mStatusController = new StatusController(this);
        mConnectionController = new ConnectionController(mServiceSettings);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mNotificationController,
                NotificationServiceConfig.NOTIFICATION_CONTROLLER_PRIORITY);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mConnectionController, NotificationServiceConfig.CONNECTION_COMPONENT_PRIORITY);
        mConnectionController.setConnectionStatusListener(this, this);
        Thread wsThread = new Thread(this::startListening);
        wsThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mBinder == null){
            Log.e(TAG, "Binder is null!");
        }
        return mBinder;
    }

    @Override
    public void onNewNotifications(JSONObject response) {
        if(response != null){
            mNotificationController.onNotificationsUpdated(response);
        }else{
            Log.e(TAG, "null response for notifications");
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected){
        if(mNotificationWebsocket == null){
            Log.w(TAG, "Notification websocket is currently null. Ignoring connection state change.");
            return;
        }
        if(!mNotificationWebsocket.getConnected() && isConnected){
            Log.d(TAG, "Not connected: restarting connection");
            startListening();
        }
    }

    private void startListening(){
        try {
            mNotificationWebsocket = mAPI.getNotificationsWebsocket(this);
            mStatusController.addComponent(
                    NotificationServiceComponents.SERVICE_COMPONENT_WEBSOCKET,
                    mNotificationWebsocket, NotificationServiceConfig.API_COMPONENT_PRIORITY);
            mAPI.getNotifications(this);
        } catch (Exception e) {
            Log.e(TAG, "Exception while starting listening:", e);
        }
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

    @Override
    public String getStatus() {
        return mStatusController.getStatusString();
    }

    @Override
    public void onPreferencesChanged() {
        if(!mServiceSettings.isWebsocketEnabled()){
            Log.i(TAG, "Websocket is no more enabled. Disconnecting websocket and stopping service");
            mNotificationWebsocket.close();
            stopForeground(true);
        }
        Log.i(TAG, "Preferences changed. Re-connecting to websocket.");
        mNotificationWebsocket.close();
        mAPI = mServiceSettings.getAPIFromSettings();
        startListening();
    }

    @Override
    public void onAccountChanged() {
        Log.i(TAG, "Account changed. Re-connecting to websocket.");
        mNotificationWebsocket.close();
        mAPI = mServiceSettings.getAPIFromSettings();
        startListening();
    }

    @Override
    public void onDestroy(){
        mConnectionController.removeConnectionListener(this);
        super.onDestroy();
    }
}