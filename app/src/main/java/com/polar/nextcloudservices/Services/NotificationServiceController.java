package com.polar.nextcloudservices.Services;

import android.content.Context;
import android.content.Intent;
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
        if(!mServiceSettings.isServiceEnabled()){
            Log.i(TAG, "Not starting service as it is not enabled");
            return null;
        }
        if(mServiceSettings.isWebsocketEnabled()){
            return NotificationWebsocketService.class;
        }else{
            return NotificationPollService.class;
        }
    }

    public void startService(Context context){
        Intent intent = new Intent(context, getServiceClass());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

}
