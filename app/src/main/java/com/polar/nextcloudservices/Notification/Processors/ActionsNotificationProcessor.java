package com.polar.nextcloudservices.Notification.Processors;

import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_CUSTOM_ACTION;
import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_FASTREPLY;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionsNotificationProcessor implements AbstractNotificationProcessor {

    public static final int priority = 2;
    private static final String KEY_CUSTOM_ACTION = "key_custom_action";
    private static final String TAG = "Notification.Processors.ActionsNotificationProcessor";

    private static PendingIntent getCustomActionIntent(Context context, JSONObject action){
        Intent intent = new Intent();
        intent.setAction(Config.NotificationEventAction);
        try {
            intent.putExtra("notification_event", NOTIFICATION_EVENT_CUSTOM_ACTION);
            intent.putExtra("action_link", action.getString("link"));
            intent.putExtra("action_method", action.getString("method"));
        } catch (JSONException e) {
            Log.e(TAG, "Can not get link or method from action provided by Nextcloud API");
            Log.e(TAG, e.toString());
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
        }else{
            return PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
    }

    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         @NonNull JSONObject rawNotification,
                                                         Context context,
                                                         NotificationService service) throws Exception {
        if(!rawNotification.has("actions")){
            return builder;
        }
        JSONArray actions = rawNotification.getJSONArray("actions");
        final int n_actions = actions.length();
        for(int i = 0; i < n_actions; ++i){
            JSONObject action = actions.getJSONObject(i);
            PendingIntent actionPendingIntent = getCustomActionIntent(context, action);
            if(actionPendingIntent == null){
                Log.w(TAG, "Can not create action for notification");
                return builder;
            }
            final String actionTitle = action.getString("label");
            NotificationCompat.Action notificationAction = new NotificationCompat.Action.Builder(
                    null,
                    actionTitle, actionPendingIntent)
                    .build();
            builder.addAction(notificationAction);
        }
        return builder;
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent,
                                    NotificationService service) {
        if(event == NOTIFICATION_EVENT_CUSTOM_ACTION){
            final String link = intent.getStringExtra("action_link");
            final String method = intent.getStringExtra("action_method");
            Thread thread = new Thread(() ->{
                try {
                    service.API.sendAction(service, link, method);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
            thread.start();
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
