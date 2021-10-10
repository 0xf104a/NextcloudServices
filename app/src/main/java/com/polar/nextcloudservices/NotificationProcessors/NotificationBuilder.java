package com.polar.nextcloudservices.NotificationProcessors;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class NotificationBuilder {
    private Vector<AbstractNotificationProcessor> processors;

    public Notification buildNotification(int id, JSONObject rawNotification, Context context) throws JSONException {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, rawNotification.getString("app"));
        for(int i=0; i<processors.size(); ++i){
            mBuilder = processors.get(i).updateNotification(id, mBuilder, mNotificationManager, rawNotification);
        }
        return mBuilder.build();
    }
    public void addProcessor(AbstractNotificationProcessor processor){
        int place=0;
        for(;place<processors.size(); ++place){
            if(processors.get(place).priority>=processor.priority){
               break;
            }
        }
        processors.insertElementAt(processor, place);
    }
}
