package com.polar.nextcloudservices.Notification;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.Processors.ActionsNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.BasicNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.NextcloudTalkProcessor;
import com.polar.nextcloudservices.Notification.Processors.OpenBrowserProcessor;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.Status;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

public class NotificationController implements NotificationEventReceiver, StatusCheckable {
    private final HashSet<Integer> active_notifications = new HashSet<>();
    private final NotificationBuilder mNotificationBuilder;
    private final Context mContext;
    private String mStatusString = "Updating settings";
    private boolean isLastSendSuccessful = false;
    private static final String TAG = "Notification.NotificationController";
    private final NotificationManager mNotificationManager;
    private final ServiceSettings mServiceSettings;

    public NotificationController(Context context, ServiceSettings settings){
        mNotificationBuilder = new NotificationBuilder();
        context.getApplicationContext().registerReceiver(
                new NotificationBroadcastReceiver(this),
                new IntentFilter(Config.NotificationEventAction));
        mContext = context;
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mServiceSettings = settings;
        registerNotificationProcessors();
    }

    private void registerNotificationProcessors(){
        for(AbstractNotificationProcessor processor : NotificationConfig.NOTIFICATION_PROCESSORS){
            mNotificationBuilder.addProcessor(processor);
        }
    }

    private void sendNotification(int notification_id, JSONObject notification){
        Log.d(TAG, "Sending notification:" + notification_id);
        active_notifications.add(notification_id);
        //FIXME: In worst case too many threads can be run
        Thread thread = new Thread(() -> {
            Notification mNotification;
            try {
                mNotification = mNotificationBuilder.buildNotification(notification_id,
                        notification, mContext, this);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse notification");
                e.printStackTrace();
                return ;
            }
            Log.d(TAG, "Will post notification now");
            mNotificationManager.notify(notification_id, mNotification);
        });
        thread.start();
    }

    public void removeNotification(int notification_id){
        Log.d(TAG, "Removing notification " + Integer.valueOf(notification_id).toString());
        mNotificationManager.cancel(notification_id);
        synchronized (active_notifications) {
            active_notifications.remove(notification_id);
        }
    }

    public void onNotificationsUpdated(JSONObject response){
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
                        sendNotification(notification_id, notification);
                    }
                }
                for (int remove_id : remove_notifications) {
                    removeNotification(remove_id);
                }
                isLastSendSuccessful = true;
            } catch (Exception e) {
                mStatusString = "Disconnected: " + e.getLocalizedMessage();
                isLastSendSuccessful = false;
                e.printStackTrace();
            }
        }
    }

    public void onNotificationEvent(NotificationEvent event, Intent intent){
        mNotificationBuilder.onNotificationEvent(event, intent, this);
    }

    @Override
    public Status getStatus(Context context) {
        if(!isLastSendSuccessful){
            return Status.Failed(mStatusString);
        }
        return Status.Ok();
    }

    public NextcloudAbstractAPI getAPI(){
        return mServiceSettings.getAPIFromSettings();
    }

    public Context getContext(){
        return mContext;
    }

    public ServiceSettings getServiceSettings(){
        return mServiceSettings;
    }
}
