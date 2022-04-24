package com.polar.nextcloudservices.Notification.Processors;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class NextcloudTalkProcessor implements AbstractNotificationProcessor {
    public final int priority = 2;
    private static final String TAG = "NotificationProcessors.NextcloudTalkProcessor";

    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context, NotificationService service) throws JSONException {

        if (!rawNotification.getString("app").equals("spreed")) {
            return builder;
        }

        PackageManager pm = context.getPackageManager();
        if (!Util.isPackageInstalled("com.nextcloud.talk2", pm)) {
            return builder;
        }

        Log.d(TAG, "Setting up talk notification");

        Intent intent = pm.getLaunchIntentForPackage("com.nextcloud.talk2");
        //intent.setComponent(new ComponentName("com.nextcloud.talk2",
        //        "com.nextcloud.talk2.activities.MainActivity"));
        PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return builder.setContentIntent(pending_intent);
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent, NotificationService service) {

    }

    @Override
    public int getPriority() {
        return priority;
    }


}
