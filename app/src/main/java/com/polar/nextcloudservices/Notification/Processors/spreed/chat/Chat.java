package com.polar.nextcloudservices.Notification.Processors.spreed.chat;

import androidx.core.app.Person;

import java.util.Vector;

public class Chat {
    public Vector<ChatMessage> messages;
    //public String chat_id;
    public Integer nc_notification_id;

    public Chat(Integer nc_notification_id) {
        this.nc_notification_id = nc_notification_id;
        messages = new Vector<>();
    }

    public void onNewMessage(String text, Person person, long timestamp, int nc_notification_id){
        messages.add(new ChatMessage(text, person, timestamp, nc_notification_id));
    }
}
