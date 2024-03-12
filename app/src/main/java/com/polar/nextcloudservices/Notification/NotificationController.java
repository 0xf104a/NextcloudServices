package com.polar.nextcloudservices.Notification;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.API.INextcloudAbstractAPI;
import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.Status;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.NoSuchElementException;

public class NotificationController implements NotificationEventReceiver, StatusCheckable {
    private final HashSet<Integer> active_notifications = new HashSet<>();
    private final NotificationBuilder mNotificationBuilder;
    private final Context mContext;
    private String mStatusString = "Updating settings";
    private boolean isLastSendSuccessful = false;
    private static final String TAG = "Notification.NotificationController";
    private final NotificationManager mNotificationManager;
    private final ServiceSettings mServiceSettings;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public NotificationController(Context context, ServiceSettings settings) {
        mNotificationBuilder = new NotificationBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.getApplicationContext().registerReceiver(
                    new NotificationBroadcastReceiver(this),
                    new IntentFilter(Config.NotificationEventAction),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.getApplicationContext().registerReceiver(
                    new NotificationBroadcastReceiver(this),
                    new IntentFilter(Config.NotificationEventAction));
        }
        mContext = context;
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mServiceSettings = settings;
        registerNotificationProcessors();
    }

    @NonNull
    public Notification getServiceNotification(){
        //Create background service notifcation
        String channelId = "__internal_backgorund_polling";
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Background polling", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }
        //Build notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext, channelId)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(mContext.getString(R.string.app_name))
                        .setPriority(-2)
                        .setOnlyAlertOnce(true)
                        .setContentText("Background connection notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(channelId);
        }
        return mBuilder.build();
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
            NotificationBuilderResult builderResult;
            Notification aNotification;
            int n_id = notification_id;
            try {
                builderResult = mNotificationBuilder.buildNotification(n_id,
                        notification, mContext, this);
                aNotification = builderResult.getNotification();
                NotificationControllerExtData data = builderResult.getExtraData();
                if(data.needOverrideId()){
                    n_id = data.getNotificationId();
                    Log.d(TAG, "Overriding " + notification_id + " by " + n_id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse notification");
                e.printStackTrace();
                return ;
            }
            Log.d(TAG, "Will post notification now");
            mNotificationManager.notify(n_id, aNotification);
        });
        thread.start();
    }

    public void removeNotification(int notification_id){
        if(notification_id < 0){
            Log.w(TAG, "Got notification id which is negative, ignoring request");
            return;
        }
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

    public void tellActionRequestFailed(){
        Toast.makeText(mContext, R.string.quick_action_failed, Toast.LENGTH_LONG).show();
    }

    public Notification getNotificationById(int id) throws NoSuchElementException {
        for(StatusBarNotification notification: mNotificationManager.getActiveNotifications()){
            if(notification.getId() == id){
                return notification.getNotification();
            }
        }
        throw new NoSuchElementException("Can not find notification with specified id: " + id);
    }

    public void postNotification(int id, Notification notification){
        mNotificationManager.notify(id, notification);
    }

    public INextcloudAbstractAPI getAPI(){
        return mServiceSettings.getAPIFromSettings();
    }

    public Context getContext(){
        return mContext;
    }

    public ServiceSettings getServiceSettings(){
        return mServiceSettings;
    }
}
