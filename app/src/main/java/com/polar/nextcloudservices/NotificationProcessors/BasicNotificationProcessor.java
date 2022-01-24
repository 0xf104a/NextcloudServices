package com.polar.nextcloudservices.NotificationProcessors;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import com.polar.nextcloudservices.Notifications.NotificationUtils;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Interfaces.AbstractNotificationProcessor;

import org.json.JSONException;
import org.json.JSONObject;

public class BasicNotificationProcessor implements AbstractNotificationProcessor {
    public final int priority = 0;
    private final static String TAG = "BasicNotificationProcessor";
    private Context mContext;

    public BasicNotificationProcessor(Context context){
        mContext = context;
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

    public String prettifyChannelName(String Name) {
        String prettyname = NotificationUtils.getTranslation(mContext, Name);
        if(!prettyname.equals(Name)){
           return prettyname;
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

    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder, NotificationManager manager, JSONObject rawNotification, Context context) throws JSONException {
        final String app = prettifyChannelName(rawNotification.getString("app"));
        String title = rawNotification.getString("subject");
        String text = rawNotification.getString("message");
        final String app_name = rawNotification.getString("app");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(app_name, app, NotificationManager.IMPORTANCE_HIGH);
            Log.d(TAG, "Creating channel");
            manager.createNotificationChannel(channel);
        }

        if(text.isEmpty()){
            text = title;
            title = app;
        }

        return builder.setSmallIcon(iconByApp(app_name))
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))

                .setChannelId(app_name);
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
