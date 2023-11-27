
package com.polar.nextcloudservices.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


import com.polar.nextcloudservices.API.INextcloudAbstractAPI;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.StatusController;

class PollTask extends AsyncTask<NotificationPollService, Void, JSONObject> {
    private static final String TAG = "Services.NotificationPollService.PollTask";
    @Override
    protected JSONObject doInBackground(NotificationPollService... services) {
        Log.d(TAG, "Checking notifications");
        INextcloudAbstractAPI api = services[0].getAPI();
        try {
            boolean hasNotifications = api.checkNewNotifications();
            if(hasNotifications) {
                return api.getNotifications(services[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not check new notifications");
            e.printStackTrace();
        }
        return null;
    }
}

public class NotificationPollService extends Service
        implements INotificationListener, INotificationService {
    // constant
    public Integer pollingInterval = null;
    public static final String TAG = "Services.NotificationPollService";
    private NotificationServiceBinder mBinder;
    private PollTimerTask task;
    public INextcloudAbstractAPI mAPI;
    private ServiceSettings mServiceSettings;
    private ConnectionController mConnectionController;
    private StatusController mStatusController;
    private NotificationController mNotificationController;
    // run on another Thread to avoid crash
    private final Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    @Override
    public String getStatus() {
        return mStatusController.getStatusString();
    }


    public void onNewNotifications(JSONObject response) {
        if(response != null) {
            mNotificationController.onNotificationsUpdated(response);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void updateTimer() {
        task.cancel();
        mTimer.purge();
        mTimer = new Timer();
        task = new PollTimerTask();
        mTimer.scheduleAtFixedRate(task, 0, pollingInterval);
    }

    private void startTimer(){
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        task = new PollTimerTask();
        mTimer.scheduleAtFixedRate(task, 0, pollingInterval);
    }

    public INextcloudAbstractAPI getAPI(){
        if(mServiceSettings == null){
            Log.wtf(TAG, "mServiceSettings is null!");
            return null;
        }
        return mServiceSettings.getAPIFromSettings();
    }

    @Override
    public void onCreate() {
        mBinder = new NotificationServiceBinder(this);
        mServiceSettings = new ServiceSettings(this);
        mAPI = mServiceSettings.getAPIFromSettings();
        pollingInterval = mServiceSettings.getPollingIntervalMs();
        Log.d(TAG, "onCreate: Set polling interval to " + pollingInterval);
        mConnectionController = new ConnectionController(mServiceSettings);
        mNotificationController = new NotificationController(this, mServiceSettings);
        mStatusController = new StatusController(this);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mConnectionController, NotificationServiceConfig.CONNECTION_COMPONENT_PRIORITY);
        mStatusController.addComponent(
                NotificationServiceComponents.SERVICE_COMPONENT_NOTIFICATION_CONTROLLER,
                mNotificationController,
                NotificationServiceConfig.NOTIFICATION_CONTROLLER_PRIORITY);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_API,
                mAPI, NotificationServiceConfig.API_COMPONENT_PRIORITY);
        startTimer();
        startForeground(1, mNotificationController.getServiceNotification());
    }

    @Override
    public void onPreferencesChanged() {
        mServiceSettings.onPreferencesChanged();
        int _pollingInterval = mServiceSettings.getPollingIntervalMs();
        onAccountChanged();
        if (_pollingInterval != pollingInterval) {
            Log.d(TAG, "Updating timer");
            pollingInterval = _pollingInterval;
            updateTimer();
        }

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying service");
        task.cancel();
        mTimer.purge();
    }




    @Override
    public void onAccountChanged(){
        mAPI = mServiceSettings.getAPIFromSettings();
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_API, mAPI,
                NotificationServiceConfig.CONNECTION_COMPONENT_PRIORITY);
    }

    class PollTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(() -> {
                if (mConnectionController.checkConnection(getApplicationContext())) {
                    new PollTask().execute(NotificationPollService.this);
                }
            });
        }

    }
}