package com.polar.nextcloudservices.Notification.Processors;

import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_DELETE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BasicNotificationProcessor implements AbstractNotificationProcessor {
    public final int priority = 0;
    private final static String TAG = "NotificationProcessors.BasicNotificationProcessor";

    public int iconByApp(String appName) {
        if (appName.equals("spreed")) {
            return R.drawable.ic_icon_foreground;
        } else if (appName.equals("deck")) {
            return R.drawable.ic_deck;
        } else {
            return R.drawable.ic_logo;
        }
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

    private PendingIntent createNotificationDeleteIntent(Context context, int id) {
        Intent intent = new Intent();
        intent.setAction(Config.NotificationEventAction);
        intent.putExtra("notification_id", id);
        intent.putExtra("notification_event", NOTIFICATION_EVENT_DELETE);
        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
    }

    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         JSONObject rawNotification,
                                                         Context context, NotificationService service) throws JSONException {
        final boolean removeOnDismiss = service.getBoolPreference("remove_on_dismiss", false);
        final String app = prettifyChannelName(rawNotification.getString("app"));
        final String title = rawNotification.getString("subject");
        final String text = rawNotification.getString("message");
        final String app_name = rawNotification.getString("app");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(app_name, app, NotificationManager.IMPORTANCE_HIGH);
            Log.d(TAG, "Creating channel");
            manager.createNotificationChannel(channel);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        final String dateStr = rawNotification.getString("datetime");
        long unixTime = 0;
        try {
            Date date = format.parse(dateStr);
            if(date == null){
                throw new ParseException("Date was not parsed: result is null", 0);
            }
            unixTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        builder = builder.setSmallIcon(iconByApp(app_name))
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(text)
                .setChannelId(app_name);
        if(unixTime != 0){
            builder.setWhen(unixTime);
        }else{
            Log.w(TAG, "unixTime is 0, maybe parse failure?");
        }
        if(removeOnDismiss){
            Log.d(TAG, "Adding intent for delete notification event");
            builder = builder.setDeleteIntent(createNotificationDeleteIntent(context, rawNotification.getInt("notification_id")));
        }
        return builder;
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent, NotificationService service) {
        if(event != NOTIFICATION_EVENT_DELETE){
            return;
        }
        int id = intent.getIntExtra("notification_id", -1);
        if(id < 0){
            Log.wtf(TAG, "Notification delete event has not provided an id of notification deleted!");
        }
        Thread thread = new Thread(() -> {
            service.API.removeNotification(service, id);
        });
        thread.start();
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
