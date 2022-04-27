package com.polar.nextcloudservices.Notification.Processors;

import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_FASTREPLY;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class NextcloudTalkProcessor implements AbstractNotificationProcessor {
    public final int priority = 2;
    private static final String TAG = "NotificationProcessors.NextcloudTalkProcessor";
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    static private PendingIntent getReplyIntent(Context context, @NonNull JSONObject rawNotification) throws JSONException {
        Intent intent = new Intent();
        intent.setAction(Config.NotificationEventAction);
        intent.putExtra("notification_id", rawNotification.getInt("notification_id"));
        intent.putExtra("notification_event", NOTIFICATION_EVENT_FASTREPLY);
        intent.putExtra("talk_chatroom", rawNotification.getString("object_id"));

        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @NonNull
    private Person getPersonFromNotification(@NonNull JSONObject rawNotification) throws JSONException {
        Person.Builder builder = new Person.Builder();
        final String key = rawNotification.getString("object_id");
        final String name = rawNotification.getJSONObject("subjectRichParameters")
                .getJSONObject("call").getString("name");
        return builder.setKey(key)
                .setName(name).build();
    }


    @Override
    public NotificationCompat.Builder updateNotification(int id, NotificationCompat.Builder builder,
                                                         NotificationManager manager,
                                                         @NonNull JSONObject rawNotification,
                                                         Context context, NotificationService service) throws JSONException {

        if (!rawNotification.getString("app").equals("spreed")) {
            return builder;
        }

        Log.d(TAG, "Setting up talk notification");

        if (rawNotification.has("object_type")) {
            if (rawNotification.getString("object_type").equals("chat")) {
                Log.d(TAG, "Talk notification of chat type, adding fast reply button");
                String replyLabel = "Reply"; //FIXME: get text from resources
                RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                        .setLabel(replyLabel)
                        .build();
                PendingIntent replyPendingIntent = getReplyIntent(context, rawNotification);
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                                "Reply", replyPendingIntent)
                                .addRemoteInput(remoteInput)
                                .build();
                builder.addAction(action);
                final String title = rawNotification.getJSONObject("subjectRichParameters")
                        .getJSONObject("call").getString("name");
                Person chat = getPersonFromNotification(rawNotification);
                SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
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
                builder.setStyle(new NotificationCompat.MessagingStyle(chat)
                        .setConversationTitle(title)
                        .addMessage(rawNotification.getString("message"), unixTime, chat));
            }
        }

        PackageManager pm = context.getPackageManager();
        if (!Util.isPackageInstalled("com.nextcloud.talk2", pm)) {
            return builder;
        }

        Intent intent = pm.getLaunchIntentForPackage("com.nextcloud.talk2");
        //intent.setComponent(new ComponentName("com.nextcloud.talk2",
        //        "com.nextcloud.talk2.activities.MainActivity"));
        PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return builder.setContentIntent(pending_intent);
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent, NotificationService service) {
        if (event == NOTIFICATION_EVENT_FASTREPLY) {
            final String chatroom = intent.getStringExtra("talk_chatroom");
            final int notification_id = intent.getIntExtra("notification_id", -1);
            if (notification_id < 0) {
                Log.wtf(TAG, "Bad notification id: " + notification_id);
                return;
            }
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput == null) {
                Log.e(TAG, "Reply event has null reply text");
                return;
            }
            final String reply = remoteInput.getCharSequence(KEY_TEXT_REPLY).toString();
            Thread thread = new Thread(() -> {
                try {
                    service.API.sendTalkReply(service, chatroom, reply);
                    service.API.removeNotification(service, notification_id);
                    service.removeNotification(notification_id);
                } catch (IOException e) {
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
