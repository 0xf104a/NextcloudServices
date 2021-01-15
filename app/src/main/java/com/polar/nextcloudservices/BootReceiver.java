package com.polar.nextcloudservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.ContextWrapper;
import android.os.Build;



public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent _intent = new Intent(context,NotificationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(_intent);
            } else {
                context.startService(_intent);
            }
            Log.i("BootReceiver", "started");
        }
    }
}
