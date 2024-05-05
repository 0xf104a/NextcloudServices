package com.polar.nextcloudservices.Notification.Processors;

// This processor is default processor for user click event
// It is used to open web page and has priority 1
// So it is executed first and can be overriden by per-app processors


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsIntent;

import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Notification.NotificationBuilderResult;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Notification.NotificationEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenBrowserProcessor implements AbstractNotificationProcessor {
    public final int priority = 1;
    private static final String TAG = "Notification.Processors.OpenBrowserProcessor";

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public NotificationBuilderResult updateNotification(int id, NotificationBuilderResult builderResult,
                                                        NotificationManager manager,
                                                        JSONObject rawNotification,
                                                        Context context, NotificationController controller) throws JSONException {
        if (!rawNotification.has("link")) {
            return builderResult;
        }

        Log.d(TAG, "Setting link for browser opening");

        CustomTabsIntent browserIntent = new CustomTabsIntent.Builder()
                .setUrlBarHidingEnabled(true)
                .setShowTitle(false)
                .setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
                .setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build();
        browserIntent.intent.setData(Uri.parse(rawNotification.getString("link")));
        browserIntent.intent.setPackage(context.getPackageName()); // Issue_78: https://developer.android.com/about/versions/14/behavior-changes-14?hl=en#safer-intents

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builderResult.builder = builderResult.builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    browserIntent.intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
            return builderResult;
        }else{
            builderResult.builder = builderResult.builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    browserIntent.intent, PendingIntent.FLAG_UPDATE_CURRENT));
            return builderResult;
        }
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent, NotificationController controller) {

    }

    @Override
    public int getPriority() {
        return priority;
    }
}
