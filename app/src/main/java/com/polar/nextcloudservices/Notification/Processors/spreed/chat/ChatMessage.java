package com.polar.nextcloudservices.Notification.Processors.spreed.chat;

import androidx.core.app.Person;

public class ChatMessage implements Comparable<ChatMessage> {
    public String text;
    public long timestamp;
    public int notification_id;
    public Person person;

    public ChatMessage(String text, Person person, long timestamp, int notification_id) {
        this.text = text;
        this.timestamp = timestamp;
        this.notification_id = notification_id;
        this.person = person;
    }

    @Override
    public int compareTo(ChatMessage o) {
        return (int)(this.timestamp - o.timestamp);
    }
}
