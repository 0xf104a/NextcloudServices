package com.polar.nextcloudservices.Notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.util.Vector;

public class NotificationBuilder {
    private final Vector<AbstractNotificationProcessor> processors;
    private final static String TAG = "Notification.NotificationBuilder";

    public NotificationBuilder(){
        processors = new Vector<>();
    }

    public NotificationBuilderResult buildNotification(int id, JSONObject rawNotification, Context context,
                                          NotificationController controller) throws Exception {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, rawNotification.getString("app"));
        NotificationBuilderResult result = new NotificationBuilderResult(builder);
        for(int i=0; i<processors.size(); ++i){
            Log.d(TAG, "Will call notification processor: " + processors.get(i).toString());
            result = processors.get(i).updateNotification(id, result, mNotificationManager,
                    rawNotification, context, controller);
        }
        return result;
    }

    public void addProcessor(AbstractNotificationProcessor processor){
        int place=0;
        if(processors.size() == 0){
            Log.w(TAG, "No processors are registered. Resulting notification would be likely invalid");
        }
        for(;place<processors.size(); ++place){
            if(processors.get(place).getPriority()>=processor.getPriority()){
               break;
            }
        }
        processors.insertElementAt(processor, place);
    }

    public void onNotificationEvent(NotificationEvent event, Intent intent,
                                    NotificationController service) {
        for(AbstractNotificationProcessor processor: processors){
            processor.onNotificationEvent(event, intent, service);
        }
    }
}
