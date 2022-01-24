package com.polar.nextcloudservices.NotificationProcessors;

// This processor is default processor for user click event
// It is used to open web page and has priority 1
// So it is executed first and can be overriden by per-app processors


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Interfaces.AbstractNotificationProcessor;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenBrowserProcessor implements AbstractNotificationProcessor {
    public final int priority = 1;
    private static final String TAG = "NotificationProcessors.OpenBrowserProcessor";

    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context) throws JSONException {
        if (!rawNotification.has("link")) {
            return builder;
        }

        Log.d(TAG, "Setting link for browser opening");
        String link = rawNotification.getString("link");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent = intent.setData(Uri.parse((link)));
        PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return builder.setContentIntent(pending_intent);
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
