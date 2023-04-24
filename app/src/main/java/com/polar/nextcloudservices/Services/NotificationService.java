
package com.polar.nextcloudservices.Services;

import android.content.IntentFilter;
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


import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;


import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.NotificationBroadcastReceiver;
import com.polar.nextcloudservices.Notification.NotificationBuilder;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.Notification.Processors.ActionsNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.BasicNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.NextcloudTalkProcessor;
import com.polar.nextcloudservices.Notification.Processors.OpenBrowserProcessor;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Services.Status.StatusController;

class PollTask extends AsyncTask<NotificationService, Void, JSONObject> {
    private final String TAG = "Service.NotificationService.PollTask";

    @Override
    protected JSONObject doInBackground(NotificationService... services) {
        return services[0].mAPI.getNotifications(services[0]);
    }
}

public class NotificationService extends Service implements PollingService {
    // constant
    public long pollingInterval = 3 * 1000; // 3 seconds
    public static final String TAG = "Services.NotificationService";
    public String status = "Disconnected";
    private Binder mBinder;
    private PollTimerTask task;
    public NextcloudAbstractAPI mAPI;
    private NotificationBuilder mNotificationBuilder;
    private ServiceSettings mServiceSettings;
    private ConnectionController mConnectionController;
    private StatusController mStatusController;

    private void registerNotificationProcessors(){
        if(mNotificationBuilder==null){
            throw new RuntimeException("registerNotificationProcessors called too early: mNotificationBuilder is null!");
        }
        //Register your notification processors here
        mNotificationBuilder.addProcessor(new BasicNotificationProcessor());
        mNotificationBuilder.addProcessor(new OpenBrowserProcessor());
        mNotificationBuilder.addProcessor(new NextcloudTalkProcessor());
        mNotificationBuilder.addProcessor(new ActionsNotificationProcessor());
    }

    private final HashSet<Integer> active_notifications = new HashSet<>();
    // run on another Thread to avoid crash
    private final Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public String getStatus() {
        return mStatusController.getStatusString();
    }

    public void onPollFinished(JSONObject response) {
        synchronized (active_notifications) {
            try {
                HashSet<Integer> remove_notifications = new HashSet<>(active_notifications);
                int notification_id;
                JSONArray notifications = response.getJSONObject("ocs").getJSONArray("data");
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                for (int i = 0; i < notifications.length(); ++i) {
                    JSONObject notification = notifications.getJSONObject(i);
                    notification_id = notification.getInt("notification_id");
                    remove_notifications.remove(notification_id);
                    if (!active_notifications.contains(notification_id)) {
                        //Handle notification
                        Log.d(TAG, "Sending notification:" + notification_id);
                        active_notifications.add(notification_id);
                        final int m_notification_id = notification_id;
                        //FIXME: In worst case too many threads can be run
                        Thread thread = new Thread(() -> {
                            Notification mNotification;
                            try {
                                mNotification = mNotificationBuilder.buildNotification(m_notification_id,
                                        notification, getBaseContext(), this);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to parse notification");
                                e.printStackTrace();
                                return ;
                            }
                            Log.d(TAG, "Will post notification now");
                            mNotificationManager.notify(m_notification_id, mNotification);
                        });
                        thread.start();
                    }
                }
                for (int remove_id : remove_notifications) {
                    Log.d(TAG, "Removing notification " + Integer.valueOf(remove_id).toString());
                    mNotificationManager.cancel(remove_id);
                    active_notifications.remove(remove_id);
                }

            } catch (Exception e) {
                this.status = "Disconnected: " + e.getLocalizedMessage();
                e.printStackTrace();
            }
        }
    }

    public void removeNotification(int id){
        Log.d(TAG, "Removing notification: " + id);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
        active_notifications.remove(id);
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

    @Override
    public void onCreate() {
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
        Notification mNotification = mBuilder.build();
        //Here we want to get Nextcloud account if it does exist
        //Otherwise we will use basic NextcloudHttpAPI
        startForeground(1, mNotification);
        mBinder = new Binder();
        //Create NotificationBuilder
        mNotificationBuilder = new NotificationBuilder();
        getApplicationContext().registerReceiver(new NotificationBroadcastReceiver(this),
                new IntentFilter(Config.NotificationEventAction));
        registerNotificationProcessors();
        mServiceSettings = new ServiceSettings(this);
        mConnectionController = new ConnectionController(mServiceSettings);
        mStatusController = new StatusController(this);
        mStatusController.addComponent(NotificationServiceComponents.SERVICE_COMPONENT_CONNECTION,
                mConnectionController);
    }

    public void onPreferencesChange() {
        mServiceSettings.onPreferencesChanged();
        int _pollingInterval = mServiceSettings.getPollingIntervalMs();
        if (_pollingInterval <= 0) {
            Log.w(TAG, "Invalid polling interval! Setting to 3 seconds.");
            _pollingInterval = 3 * 1000;
        }

        if (_pollingInterval != pollingInterval) {
            Log.d(TAG, "Updating timer");
            pollingInterval = _pollingInterval;
            updateTimer();
        }

    }

    public void onNotificationEvent(NotificationEvent event, Intent intent){
        mNotificationBuilder.onNotificationEvent(event, intent, this);
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