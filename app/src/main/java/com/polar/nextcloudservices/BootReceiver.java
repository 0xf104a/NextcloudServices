package com.polar.nextcloudservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Build;

import com.polar.nextcloudservices.Services.NotificationPollService;
import com.polar.nextcloudservices.Services.NotificationServiceController;
import com.polar.nextcloudservices.Services.Settings.ServiceSettings;


public class BootReceiver extends BroadcastReceiver {

    private void startService(Context context){
        NotificationServiceController controller =
                new NotificationServiceController(new ServiceSettings(context));
        controller.startService(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startService(context);
            Log.i("BootReceiver", "received boot completed");
        }
    }
}
