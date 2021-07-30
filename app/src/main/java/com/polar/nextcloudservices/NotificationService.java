
package com.polar.nextcloudservices;

import android.app.*;
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
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Binder;


import androidx.annotation.RequiresApi;
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
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.net.*;

import android.util.Base64;


import javax.net.ssl.HttpsURLConnection;

class PollTask extends AsyncTask<NotificationService, Void, JSONObject> {
    private final String TAG = "NotifcationService.PollTask";
    private final String UA = "NextcloudServices/" + BuildConfig.VERSION_NAME;

    private String getAuth(String user, String password) {
        //Log.d("NotificationService.PollTask",user+":"+password);
        return Base64.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT).toString();
    }

    @Override
    protected JSONObject doInBackground(NotificationService... services) {
        try {
            String baseUrl = services[0].server;
            if (baseUrl.startsWith("https://"))
                baseUrl = baseUrl.substring(8);
            if (baseUrl.startsWith("http://"))
                baseUrl = baseUrl.substring(7);

            String endpoint = "https://" + baseUrl + "/ocs/v2.php/apps/notifications/api/v2/notifications";
            Log.d(TAG, endpoint);
            URL url = new URL(endpoint);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + getAuth(services[0].username, services[0].password));
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("User-agent", UA);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(5000);
            //conn.setRequestMethod("GET");
            Log.d(TAG, conn.getRequestProperties().toString());
            //conn.setDoOutput(true);
            conn.setDoInput(true);

            //OutputStream os = conn.getOutputStream();
            //os.close();
            String responseCode = Integer.toString(conn.getResponseCode());
            Log.d(TAG, "--> https://" + baseUrl + "/ocs/v2.php/apps/notifications/api/v2/notifications -- " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder("");
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            Log.d(TAG, buffer.toString());
            JSONObject response = new JSONObject(buffer.toString());
            services[0].onPollFinished(response);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            services[0].status = "Disconnected: server has sent bad response: " + e.getLocalizedMessage();
            return null;
        } catch (java.io.FileNotFoundException e){
            e.printStackTrace();
            services[0].status = "Disconnected: File not found: check your credentials and Nextcloud instance.";
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error while getting response");
            e.printStackTrace();
            services[0].status = "Disconnected: I/O error: "+e.getLocalizedMessage();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            services[0].status = "Disconnected: "+e.getLocalizedMessage();
            return null;
        }
    }
}

public class  NotificationService extends Service {
    // constant
    public static final long NOTIFY_INTERVAL = 3 * 1000; // 3 seconds
    public static final String TAG = "NotificationService";
    public String server = "";
    public String username = "";
    public String password = "";
    public String status = "Disconnected";
    private Binder binder;

    private final HashSet<Integer> active_notifications = new HashSet<>();
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public String getStatus() {
        return this.status;
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }  else {
            Network[] networks = connectivity.getAllNetworks();
            for (Network network : networks) {
                NetworkInfo info = connectivity.getNetworkInfo(network);
                if(info != null) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
            final NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
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
        mTimer.scheduleAtFixedRate(new PollTimerTask(), 0, NOTIFY_INTERVAL);
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

        //mNotificationManager.notify(1526756640,mNotification);
        startForeground(1, mNotification);
        binder = new Binder();

    }

    private String getPreference(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, "<none>");
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
                    if(checkInternetConnection(getApplicationContext())) {
                        new PollTask().execute(NotificationService.this);
                    }else{
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