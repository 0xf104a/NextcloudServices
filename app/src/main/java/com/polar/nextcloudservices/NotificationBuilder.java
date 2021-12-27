package com.polar.nextcloudservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polar.nextcloudservices.AbstractNotificationProcessor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class NotificationBuilder {
    private Vector<AbstractNotificationProcessor> processors;
    private final static String TAG="NotificationBuilder";

    public NotificationBuilder(){
        processors = new Vector<>();
    }

    public Notification buildNotification(int id, JSONObject rawNotification, Context context) throws JSONException {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, rawNotification.getString("app"));
        for(int i=0; i<processors.size(); ++i){
            Log.d(TAG, "Will call notification processor: "+processors.get(i).toString());
            mBuilder = processors.get(i).updateNotification(id, mBuilder, mNotificationManager,
                    rawNotification, context);
        }
        return mBuilder.build();
    }
    public void addProcessor(AbstractNotificationProcessor processor){
        int place=0;
        for(;place<processors.size(); ++place){
            if(processors.get(place).getPriority()>=processor.getPriority()){
               break;
            }
        }
        processors.insertElementAt(processor, place);
    }
}
