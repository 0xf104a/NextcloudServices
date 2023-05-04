package com.polar.nextcloudservices.Notification;

import android.app.Notification;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

public class NotificationBuilderResult {
    public NotificationControllerExtData extraData;
    public NotificationCompat.Builder builder;

    public NotificationBuilderResult(NotificationCompat.Builder builder){
        this.builder = builder;
        this.extraData = new NotificationControllerExtData();
    }

    public Notification getNotification(){
        return builder.build();
    }

    public NotificationControllerExtData getExtraData(){
        return extraData;
    }
}
