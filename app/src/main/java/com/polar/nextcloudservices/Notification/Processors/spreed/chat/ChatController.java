package com.polar.nextcloudservices.Notification.Processors.spreed.chat;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import java.util.HashMap;

/**
 * A generic controller of a chat logic.
 * It stores a history of messages per converstation and also allows to cancel, i.e.
 * remove conversation by message id.
 */
public class ChatController {
    private final HashMap<String, Chat> chat_by_room;
    private final HashMap<Integer, Chat> chat_by_notification_id;
    private final HashMap<String, Integer> notification_id_by_room;

    private final static String TAG = "ChatController";

    public ChatController() {
        chat_by_room = new HashMap<>();
        chat_by_notification_id = new HashMap<>();
        notification_id_by_room = new HashMap<>();
    }

    private Chat getChat(String room, int nc_notification_id){
        if(!chat_by_room.containsKey(room)){
            Chat chat = new Chat(nc_notification_id);
            chat_by_room.put(room, chat);
            chat_by_notification_id.put(nc_notification_id, chat);
            notification_id_by_room.put(room, nc_notification_id);
        }
        return chat_by_room.get(room);
    }

    public int getNotificationIdByRoom(String room){
        return notification_id_by_room.get(room);
    }

    public void onNewMessageReceived(String room, String text,
                                     Person person,
                                     long timestamp, int nc_notification_id){
        synchronized (chat_by_room) {
            Chat chat = getChat(room, nc_notification_id);
            chat.onNewMessage(text, person, timestamp, nc_notification_id);
            chat_by_notification_id.put(nc_notification_id, chat);
        }
    }

    public Chat getChatByRoom(String room){
        return chat_by_room.get(room);
    }

    public NotificationCompat.MessagingStyle addChatRoomMessagesToStyle(NotificationCompat.MessagingStyle style,
                                                                        String room){
        Chat chat = chat_by_room.get(room);
        if(chat == null){
            Log.wtf(TAG, "Requested non-existent room or null is in chat_by_room map");
            return null;
        }
        for(ChatMessage message : chat.messages){
            style = style.addMessage(message.text, message.timestamp, message.person);
        }
        return style;
    }
}
