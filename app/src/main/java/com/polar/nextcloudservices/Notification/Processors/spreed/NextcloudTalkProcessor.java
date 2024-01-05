package com.polar.nextcloudservices.Notification.Processors.spreed;

import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_DELETE;
import static com.polar.nextcloudservices.Notification.NotificationEvent.NOTIFICATION_EVENT_FASTREPLY;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;

import com.polar.nextcloudservices.API.INextcloudAbstractAPI;
import com.polar.nextcloudservices.Config;
import com.polar.nextcloudservices.Notification.AbstractNotificationProcessor;
import com.polar.nextcloudservices.Notification.NotificationBuilderResult;
import com.polar.nextcloudservices.Notification.NotificationController;
import com.polar.nextcloudservices.Notification.NotificationEvent;
import com.polar.nextcloudservices.Notification.Processors.spreed.chat.Chat;
import com.polar.nextcloudservices.Notification.Processors.spreed.chat.ChatController;
import com.polar.nextcloudservices.Notification.Processors.spreed.chat.ChatMessage;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class NextcloudTalkProcessor implements AbstractNotificationProcessor {
    public final int priority = 2;
    private static final String TAG = "Notification.Processors.NextcloudTalkProcessor";
    private static final String KEY_TEXT_REPLY = "key_text_reply";
    private final ChatController mChatController;

    public NextcloudTalkProcessor() {
        mChatController = new ChatController();
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    static private PendingIntent getReplyIntent(Context context,
                                                @NonNull JSONObject rawNotification) throws JSONException {
        Intent intent = new Intent();
        int notification_id = rawNotification.getInt("notification_id");
        intent.setAction(Config.NotificationEventAction);
        intent.putExtra("notification_id", rawNotification.getInt("notification_id"));
        intent.putExtra("notification_event", NOTIFICATION_EVENT_FASTREPLY);
        String[] link = rawNotification.getString("link").split("/"); // use provided link to extract talk chatroom id
        intent.putExtra("talk_chatroom", cleanUpChatroom(link[link.length-1]));
        intent.putExtra("talk_link", cleanUpChatroom(rawNotification.getString("link")));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(
                    context,
                    notification_id,
                    intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
        }else{
            return PendingIntent.getBroadcast(
                    context,
                    notification_id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
    }

    private Person getUserPerson(){
        Person.Builder builder = new Person.Builder();
        builder.setName("You");
        return builder.build();
    }

    @NonNull
    private Person getPersonFromNotification(@NonNull NotificationController controller,
                                             @NonNull JSONObject rawNotification) throws Exception {
        Person.Builder builder = new Person.Builder();
        if(rawNotification.getJSONObject("subjectRichParameters").has("user")){
            JSONObject user = rawNotification.getJSONObject("subjectRichParameters")
                    .getJSONObject("user");
            final String name = user.getString("name");
            final String id = user.getString("id");
            builder.setKey(id).setName(name);
            Bitmap image = controller.getAPI().getUserAvatar(id);
            IconCompat compat = IconCompat.createWithAdaptiveBitmap(image);
            builder.setIcon(compat);
            return builder.build();
        }else {
            final String key = rawNotification.getString("object_id");
            builder.setKey(key);
            final String name = rawNotification.getJSONObject("subjectRichParameters")
                    .getJSONObject("call").getString("name");
            //NOTE: Nextcloud Talk does not seem to provide ability for setting avatar for calls
            //      so it is not fetched here
            return builder.setName(name).build();
        }
    }

    private NotificationCompat.Builder setCustomTabsIntent(Context context,
                                                           NotificationCompat.Builder builder,
                                                           String link) {
        CustomTabsIntent browserIntent = new CustomTabsIntent.Builder()
                .setUrlBarHidingEnabled(true)
                .setShowTitle(false)
                .setStartAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
                .setExitAnimations(context, android.R.anim.fade_in, android.R.anim.fade_out)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build();
        browserIntent.intent.setData(Uri.parse(link));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    browserIntent.intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        }else{
            return builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    browserIntent.intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private NotificationCompat.Builder setTalkOpenIntent(Context context,
                                                         NotificationCompat.Builder builder){
        PackageManager pm = context.getPackageManager();
        if (!CommonUtil.isPackageInstalled("com.nextcloud.talk2", pm)) {
            Log.w(TAG, "Expected to find com.nextcloud.talk2 installed, but package was not found");
            return builder;
        }
        Log.d(TAG, "Setting up talk notification open intent");

        Intent intent = pm.getLaunchIntentForPackage("com.nextcloud.talk2");
        PendingIntent pending_intent;
        pending_intent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return  builder.setContentIntent(pending_intent);
    }

    private NotificationCompat.Builder setOpenIntent(NotificationController controller,
                                                     NotificationCompat.Builder builder,
                                                     Context context, String link){
        ServiceSettings settings = controller.getServiceSettings();
        if(settings.getSpreedOpenedInBrowser()){
            return setCustomTabsIntent(context, builder, link);
        } else {
            PackageManager pm = context.getPackageManager();
            if (!CommonUtil.isPackageInstalled("com.nextcloud.talk2", pm)) {
                Log.w(TAG, "Expected to find com.nextcloud.talk2 installed, but package was not found");
                return setCustomTabsIntent(context, builder, link);
            }
            return setTalkOpenIntent(context, builder);
        }
    }


    private NotificationBuilderResult setMessagingChatStyle(NotificationController controller,
                                                             NotificationBuilderResult builderResult,
                                                             @NonNull JSONObject rawNotification) throws Exception {
        Person person = getPersonFromNotification(controller, rawNotification);
        final String room = cleanUpChatroom(rawNotification.getString("link"));
        final String title = rawNotification.getJSONObject("subjectRichParameters")
                .getJSONObject("call").getString("name");
        final String text = rawNotification.getString("message");
        int nc_notification_id = rawNotification.getInt("notification_id");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
        final String dateStr = rawNotification.getString("datetime");
        long unixTime = 0;
        try {
            Date date = format.parse(dateStr);
            if (date == null) {
                throw new ParseException("Date was not parsed: result is null", 0);
            }
            unixTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mChatController.onNewMessageReceived(room, text, person, unixTime, nc_notification_id);
        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(person);
        style = mChatController.addChatRoomMessagesToStyle(style, room);
        style.setConversationTitle(title);
        int notification_id = mChatController.getNotificationIdByRoom(room);
        builderResult.extraData.setNotificationIdOverride(notification_id);
        builderResult.builder = builderResult.builder.setStyle(style);
        return builderResult;
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public NotificationBuilderResult updateNotification(int id, NotificationBuilderResult builderResult,
                                                        NotificationManager manager,
                                                        @NonNull JSONObject rawNotification,
                                                        Context context, NotificationController controller) throws Exception {

        if (!rawNotification.getString("app").equals("spreed")) {
            return builderResult;
        }

        Log.d(TAG, "Setting up talk notification");

        if (rawNotification.has("object_type")) {
            if (rawNotification.getString("object_type").equals("chat")) {
                Log.d(TAG, "Talk notification of chat type, adding fast reply button");
                String replyLabel = context.getString(R.string.talk_fast_reply);
                RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                        .setLabel(replyLabel)
                        .build();
                PendingIntent replyPendingIntent = getReplyIntent(context, rawNotification);
                final String fastreply_title = context.getString(R.string.talk_fast_reply);
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                                fastreply_title, replyPendingIntent)
                                .addRemoteInput(remoteInput)
                                .setAllowGeneratedReplies(true)
                                .build();
                builderResult.builder.addAction(action);
                if (rawNotification.getString("messageRich").equals("{file}") && rawNotification
                        .getJSONObject("messageRichParameters")
                        .getJSONObject("file")
                        .getString("mimetype").startsWith("image/")) {
                    Bitmap imagePreview = controller.getAPI().getImagePreview(rawNotification
                            .getJSONObject("messageRichParameters")
                            .getJSONObject("file").getString("id"));
                    builderResult.builder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(imagePreview));
                } else {
                    setMessagingChatStyle(controller, builderResult,
                            rawNotification);
                }
            }
        }
        builderResult.builder = setOpenIntent(controller, builderResult.builder, context,
                rawNotification.getString("link"));
        return builderResult;
    }

    private static String cleanUpChatroom(@NonNull String chatroom){
        String[] splits =  chatroom.split("#");
        if(splits.length == 0){
            return null;
        } else {
            return splits[0];
        }
    }

    private void onFastReply(Intent intent, NotificationController controller){
        final String chatroom =
                cleanUpChatroom(
                        Objects.requireNonNull(intent.getStringExtra("talk_chatroom"))); // the string send by spreed is chatroomid
        final String chatroom_link = cleanUpChatroom(
                Objects.requireNonNull(intent.getStringExtra("talk_link")));
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
        final String reply =
                Objects.requireNonNull(remoteInput.getCharSequence(KEY_TEXT_REPLY)).toString();
        INextcloudAbstractAPI api = controller.getAPI();
        Thread thread = new Thread(() -> {
            try {
                api.sendTalkReply(chatroom, reply);
                appendQuickReply(controller,
                        mChatController.getNotificationIdByRoom(chatroom_link), reply);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                ///controller.tellActionRequestFailed();
            }
        });
        thread.start();
    }

    private void onDeleteNotification(Intent intent, NotificationController controller){
        //NOTE: we actually can not get here if remove on dismiss disabled
        //      so we may safely ignore checking settings
        final int notification_id = intent.getIntExtra("notification_id", -1);
        if(notification_id == -1){
            Log.e(TAG, "Invalid notification id, can not properly handle notification deletion");
            return;
        }
        Chat chat = mChatController.getChatByNotificationId(notification_id);
        if(chat == null){
            Log.wtf(TAG, "Can not find chat by notification id " + notification_id);
            return;
        }
        INextcloudAbstractAPI api = controller.getAPI();
        for(ChatMessage message : chat.messages){
            Thread thread = new Thread(() -> {
                try {
                    api.removeNotification(message.notification_id);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
            thread.start();
        }
        mChatController.removeChat(chat);
    }

    @Override
    public void onNotificationEvent(NotificationEvent event, Intent intent,
                                    NotificationController controller) {
        if (event == NOTIFICATION_EVENT_FASTREPLY) {
            onFastReply(intent, controller);
        } else if(event == NOTIFICATION_EVENT_DELETE){
            onDeleteNotification(intent, controller);
        }
    }

    private void appendQuickReply(NotificationController controller,
                                  int notification_id, String text){
        Notification notification = controller.getNotificationById(notification_id);
        Context context = controller.getContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notification);
        NotificationCompat.MessagingStyle style = NotificationCompat
                .MessagingStyle.extractMessagingStyleFromNotification(notification);
        if(style == null){
            Log.wtf(TAG, "appendQuickReply: got null style");
            return;
        }
        style.addMessage(text, CommonUtil.getTimestamp(), getUserPerson());
        final String room = mChatController.getChatByNotificationId(notification_id).room;
        mChatController.onNewMessageReceived(room, text, getUserPerson(),
                CommonUtil.getTimestamp(), -1);
        notification = builder.setStyle(style).build();
        controller.postNotification(notification_id, notification);
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
