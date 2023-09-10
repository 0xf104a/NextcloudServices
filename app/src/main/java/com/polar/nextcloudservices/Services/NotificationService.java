
package com.polar.nextcloudservices.Services;

import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.StatusController;

class PollTask extends AsyncTask<NotificationService, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(NotificationService... services) {
        return services[0].getAPI().getNotifications(services[0]);
    }
}

public class NotificationService extends Service implements PollUpdateListener {
    // constant
    public Integer pollingInterval = null;
    public static final String TAG = "Services.NotificationService";
    private Binder mBinder;
    private PollTimerTask task;
    public NextcloudAbstractAPI mAPI;
    private ServiceSettings mServiceSettings;
    private ConnectionController mConnectionController;
    private StatusController mStatusController;
    private NotificationController mNotificationController;
    // run on another Thread to avoid crash
    private final Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public String getStatus() {
        return mStatusController.getStatusString();
    }

    public void onPollFinished(JSONObject response) {
        mNotificationController.onNotificationsUpdated(response);
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

    @NonNull
    private Notification getPollingNotification(){
        //Create background service notifcation
        String channelId = "__internal_backgorund_polling";
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Background polling", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }
        //Build notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(getString(R.string.app_name))
                        .setPriority(-2)
                        .setOnlyAlertOnce(true)
                        .setContentText("Background connection notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(channelId);
        }
        return mBuilder.build();
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

    public NextcloudAbstractAPI getAPI(){
        if(mServiceSettings == null){
            Log.wtf(TAG, "mServiceSettings is null!");
            return null;
        }
        return mServiceSettings.getAPIFromSettings();
    }

    @Override
    public void onCreate() {
        mBinder = new Binder();
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
        startForeground(1, getPollingNotification());
    }

    public void onPreferencesChange() {
        mServiceSettings.onPreferencesChanged();
        int _pollingInterval = mServiceSettings.getPollingIntervalMs();
        updateAccounts();
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


    public class Binder extends android.os.Binder {
        // Returns current status string of a service
        public String getServiceStatus() {
            return getStatus();
        }

        // Runs re-check of preferences, can be called from activities
        public void onPreferencesChanged() {
            onPreferencesChange();
        }

        // Update API class when accounts state change
        public void onAccountChanged() {
            updateAccounts();
        }
    }

    public void updateAccounts(){
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
                    new PollTask().execute(NotificationService.this);
                }
            });
        }

    }
}