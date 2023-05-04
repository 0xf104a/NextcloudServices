package com.polar.nextcloudservices.Notification.Processors.spreed.chat;

import androidx.core.app.Person;

import java.util.Vector;

public class Chat {
    public final Vector<ChatMessage> messages;
    public String room;
    public Integer nc_notification_id;


    public Chat(Integer nc_notification_id, String room) {
        this.nc_notification_id = nc_notification_id;
        messages = new Vector<>();
        this.room = room;
    }

    public void onNewMessage(String text, Person person, long timestamp, int nc_notification_id){
        messages.add(new ChatMessage(text, person, timestamp, nc_notification_id));
    }
}
