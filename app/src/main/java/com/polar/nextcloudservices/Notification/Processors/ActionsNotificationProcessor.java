package com.polar.nextcloudservices.Notification.Processors;

import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_CUSTOM_ACTION;

import android.annotation.SuppressLint;
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
import com.polar.nextcloudservices.Notification.NotificationBuilderResult;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.Utils.CommonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionsNotificationProcessor implements AbstractNotificationProcessor {

    public static final int priority = 2;
    private static final String TAG = "Notification.Processors.ActionsNotificationProcessor";
    private static final String[] IGNORED_APPS = {"spreed"};

    @SuppressLint("UnspecifiedImmutableFlag")
    private static PendingIntent getCustomActionIntent(Context context,
                                                       JSONObject action, int requestCode){
        Intent intent = new Intent();
        intent.setAction(Config.NotificationEventAction);
        try {
            intent.putExtra("notification_event", NOTIFICATION_EVENT_CUSTOM_ACTION);
            String link = action.getString("link");
            final String type = action.getString("type");
            link = CommonUtil.cleanUpURLIfNeeded(link);
            if(link == null){
                Log.e(TAG, "Nextcloud provided bad link for action");
                return null;
            }
            intent.putExtra("action_link", link);
            intent.putExtra("action_method", type);
            intent.setPackage(context.getPackageName()); // Issue 78 --> https://developer.android.com/about/versions/14/behavior-changes-14?hl=en#safer-intents
        } catch (JSONException e) {
            Log.e(TAG, "Can not get link or method from action provided by Nextcloud API");
            e.printStackTrace();
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
        }else{
            return PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
    }

    @Override
    public NotificationBuilderResult updateNotification(int id, NotificationBuilderResult builderResult,
                                                         NotificationManager manager,
                                                         @NonNull JSONObject rawNotification,
                                                         Context context,
                                                         NotificationController controller) throws Exception {
        final String appName = rawNotification.getString("app");
        if (CommonUtil.isInArray(appName, IGNORED_APPS)) {
            Log.d(TAG, appName + " is ignored");
            return builderResult;
        } else {
            Log.d(TAG, appName + " is not ignored");
        }
        if(!rawNotification.has("actions")){
            return builderResult;
        }
        JSONArray actions = rawNotification.getJSONArray("actions");
        final int n_actions = actions.length();
        for(int i = 0; i < n_actions; ++i){
            JSONObject action = actions.getJSONObject(i);
            PendingIntent actionPendingIntent = getCustomActionIntent(context, action, i);
            if(actionPendingIntent == null){
                Log.w(TAG, "Can not create action for notification");
                return builderResult;
            }
            final String actionTitle = action.getString("label");
            NotificationCompat.Action notificationAction = new NotificationCompat.Action.Builder(
                    null,
                    actionTitle, actionPendingIntent)
                    .build();
            builderResult.builder.addAction(notificationAction);
        }
        return builderResult;
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent,
                                    NotificationController controller) {
        if(event == NOTIFICATION_EVENT_CUSTOM_ACTION){
            final String link = intent.getStringExtra("action_link");
            final String method = intent.getStringExtra("action_method");
            Log.d(TAG, method + " " + link);
            Thread thread = new Thread(() -> {
                try {
                    controller.getAPI().sendAction(link, method);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    controller.tellActionRequestFailed();
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
