
package com.polar.nextcloudservices;

import android.content.SharedPreferences;
import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Network;


import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.net.*;

import android.util.Base64;


import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import javax.net.ssl.HttpsURLConnection;

class PollTask extends AsyncTask<NotificationService, Void, JSONObject> {
    private final String TAG = "NotifcationService.PollTask";

    @Override
    protected JSONObject doInBackground(NotificationService... services) {
        return services[0].API.getNotifications(services[0]);
    }
}

public class NotificationService extends Service {
    // constant
    public long pollingInterval = 3 * 1000; // 3 seconds
    public static final String TAG = "NotificationService";
    public String server = "";
    public String username = "";
    public String password = "";
    public String status = "Disconnected";
    public boolean useHttp = false;
    public boolean allowRoaming = false;
    public boolean allowMetered = false;
    private Binder binder;
    private PollTimerTask task;
    public NextcloudAbstractAPI API;

    private NextcloudAPI.ApiConnectedListener apiCallback = new NextcloudAPI.ApiConnectedListener() {
        @Override
        public void onConnected() {
            // ignore this one… see 5)
        }

        @Override
        public void onError(Exception ex) {
            status = "Disconnected: "+ex.getLocalizedMessage();
            ex.printStackTrace();
        }
    };

    private final HashSet<Integer> active_notifications = new HashSet<>();
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public String getStatus() {
        return this.status;
    }

    public boolean checkInternetConnection(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            //We need to check only active network state
            final NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();

            if(activeNetwork != null){
                if(activeNetwork.isConnected()){
                    if(activeNetwork.isRoaming()) {
                        //Log.d(TAG, "Network is in roaming");
                        return allowRoaming;
                    }else if(connectivity.isActiveNetworkMetered()){
                        //Log.d(TAG, "Network is metered");
                        return allowMetered;
                    }else{
                        //Log.d(TAG, "Network is unmetered");
                        return true;
                    }
                }
            }
            //activeNetwork is null
            return false;
        }
    }

    public int iconByApp(String appName) {
        if (appName.equals("spreed")) {
            return R.drawable.ic_icon_foreground;
        } else if (appName.equals("deck")) {
            return R.drawable.ic_deck;
        } else {
            return R.drawable.ic_logo;
        }
    }

    private void notificationSend(int id, String title, String text, String app, String app_name) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(app, app, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(getBaseContext(), app)
                .setSmallIcon(iconByApp(app_name))
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(text)
                .build();
        mNotificationManager.notify(id, notification);
    }

    public static String prettifyChannelName(String Name) {
        if (Name.equals("updatenotification")) {
            return "Update notifications";
        }
        if (Name.equals("spreed")) {
            return "Nextcloud talk";
        }
        String[] parts = Name.split("_");
        StringBuilder nice_name = new StringBuilder();
        for (String part : parts) {
            nice_name.append(part);
        }
        String result = nice_name.toString();
        result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        return result;
    }

    public void onPollFinished(JSONObject response) {
        synchronized (active_notifications) {
            try {
                HashSet<Integer> remove_notifications = new HashSet<>(active_notifications);
                int notification_id;
                JSONArray notifications = response.getJSONObject("ocs").getJSONArray("data");
                for (int i = 0; i < notifications.length(); ++i) {
                    JSONObject notification = notifications.getJSONObject(i);
                    notification_id = notification.getInt("notification_id");
                    remove_notifications.remove(notification_id);
                    if (!active_notifications.contains(notification_id)) {
                        //Handle notification
                        Log.d(TAG, "Sending notification:" + notification_id);
                        active_notifications.add(notification_id);
                        notificationSend(notification_id, notification.getString("subject"),
                                notification.getString("message"),
                                prettifyChannelName(notification.getString("app")),
                                notification.getString("app"));
                    }
                }
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                for (int remove_id : remove_notifications) {
                    Log.d(TAG, "Removing notification " + Integer.valueOf(remove_id).toString());
                    mNotificationManager.cancel(remove_id);
                    active_notifications.remove(remove_id);
                }
                this.status = "Connected";

            } catch (Exception e) {
                this.status = "Disconnected: " + e.getLocalizedMessage();
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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
        try {
            final SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(getApplicationContext());
            NextcloudAPI mNextcloudAPI = new NextcloudAPI(this, ssoAccount, new GsonBuilder().create(), apiCallback);
            API = new NextcloudSSOAPI(mNextcloudAPI);
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            //We do not have an account -> use HTTP API
            Log.i(TAG, "No nextcloud account was found.");
            API = new NextcloudHttpAPI();
        }
        startForeground(1, mNotification);
        binder = new Binder();

    }

    private String getPreference(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, "<none>");
    }

    private Integer getIntPreference(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(key, Integer.MIN_VALUE);
    }

    private boolean getBoolPreference(String key, boolean fallback) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(key, fallback);
    }

    public void onPreferencesChange(){
        int _pollingInterval = getIntPreference("polling_interval") * 1000;
        if (_pollingInterval <= 0) {
            Log.w(TAG, "Invalid polling interval! Setting to 3 seconds.");
            _pollingInterval = 3 * 1000;
        }

        if (_pollingInterval != pollingInterval) {
            Log.d(TAG,"Updating timer");
            pollingInterval = _pollingInterval;
            updateTimer();
        }

    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Destroying service");
        task.cancel();
        mTimer.purge();
    }


    public class Binder extends android.os.Binder {
        public String getServiceStatus() {
            return getStatus();
        }
    }

    class PollTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    username = getPreference("login");
                    password = getPreference("password");
                    server = getPreference("server");
                    useHttp = getBoolPreference("insecure_connection", false);
                    allowRoaming = getBoolPreference("allow_roaming", false);
                    allowMetered = getBoolPreference("allow_metered", false);


                    //FIXME: Should call below method only when prefernces updated
                    onPreferencesChange();

                    if (checkInternetConnection(getApplicationContext())) {
                        new PollTask().execute(NotificationService.this);
                    } else {
                        status = "Disconnected: no network available";
                    }
                }

            });
        }

        private String getDateTime() {
            // get date time in custom format
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
            return sdf.format(new Date());
        }


    }
}