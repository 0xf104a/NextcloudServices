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
    private Class<?> lastServiceClass;
    private static final String TAG = "Services.NotificationServiceController";

    public NotificationServiceController(ServiceSettings serviceSettings){
        mServiceSettings = serviceSettings;
        lastServiceClass = getServiceClass();
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
        Class<?> serviceClass = getServiceClass();
        Log.d(TAG, "Class: " + serviceClass);
        if(serviceClass == null){
            Log.w(TAG, "Can not stop service: we do not know its class");
            return;
        }
        context.stopService(new Intent(context, serviceClass));
    }

    public void onServiceClassChange(Context context){
        Log.d(TAG, "onServiceClassChange: old class = " + lastServiceClass);
        Class<?> serviceClass = getServiceClass();
        Log.d(TAG, "onServiceClassChange: new class = " + serviceClass);
        if(lastServiceClass == serviceClass){
            Log.w(TAG, "Service class is unchanged, doing nothing");
        }
        //Stop old service
        Log.i(TAG, "Stopping service...");
        Log.d(TAG, "Class: " + lastServiceClass);
        context.stopService(new Intent(context, lastServiceClass));
        lastServiceClass = serviceClass;
        //Start new service
        startService(context);
    }

    public void bindService(Context context, ServiceConnection connection){
        Log.d(TAG, "binding service");
        Class<?> serviceClass = getServiceClass();
        context.bindService(new Intent(context, serviceClass),
                connection, 0);
    }

    public void restartService(Context context){
        Log.i(TAG, "Restarting service");
        stopService(context);
        startService(context);
    }

}
