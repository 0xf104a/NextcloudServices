package com.polar.nextcloudservices.Services;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.util.Log;

import com.polar.nextcloudservices.Services.Settings.ServiceSettings;

/**
 * Implements logic of choosing between services
 */
public class NotificationServiceController {
    private final ServiceSettings mServiceSettings;
    private static final String TAG = "Services.NotificationServiceController";

    public NotificationServiceController(ServiceSettings serviceSettings){
        mServiceSettings = serviceSettings;
    }

    public Class<?> getServiceClass(){
        if(mServiceSettings.isWebsocketEnabled()){
            return NotificationWebsocketService.class;
        }else{
            return NotificationPollService.class;
        }
    }

    public void startService(Context context){
        Class<?> serviceClass = getServiceClass();
        Log.i(TAG, "Starting service...");
        Log.d(TAG, "Class: " + serviceClass);
        Intent intent = new Intent(context, serviceClass);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void stopService(Context context){
        Log.i(TAG, "Stopping service...");
        if(getServiceClass() == null){
            Log.w(TAG, "Can not stop service: we do not know its class");
            return;
        }
        context.stopService(new Intent(context, getServiceClass()));
    }

    public void bindService(Context context, ServiceConnection connection){
        context.bindService(new Intent(context.getApplicationContext(),
                        NotificationPollService.class),
                connection, 0);
    }

    public void restartService(Context context){
        Log.i(TAG, "Restarting service");
        stopService(context);
        startService(context);
    }

}
