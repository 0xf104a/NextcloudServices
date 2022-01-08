package com.polar.nextcloudservices.settings;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.databinding.ActivityMainBinding;
import com.polar.nextcloudservices.ui.settings.SettingsFragment;

public class NotificationServiceConnection implements ServiceConnection {
    private final String TAG = "SettingsActivity.NotificationServiceConnection";
    private NotificationService.Binder mService;
    public boolean isConnected = false;

    public NotificationServiceConnection() {}

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof NotificationService.Binder) {
            mService = (NotificationService.Binder) service;
            isConnected = mService.getServiceStatus() == NotificationService.STATE.CONNECTED;
        } else {
            Log.wtf(TAG, "Bad Binder type passed!");
            throw new RuntimeException("Expected NotificationService.Binder");
        }
    }

    public void updateStatus() {
        if(mService == null){
            isConnected = false;
            Log.w(TAG, "No service instantiated.");
            return;
        }
        isConnected = mService.getServiceStatus() == NotificationService.STATE.CONNECTED;
        if (!isConnected) {
            Log.w(TAG, "Service has already disconnected");
        } else {
            Log.w(TAG, "Service: "+mService.getServiceStatus());
        }
    }

    public void updateConnectionStateIndicator(ActivityMainBinding binding){
        updateStatus();
        if(isConnected){
            binding.appBarMain.connectionState.setImageResource(R.drawable.ic_baseline_cloud_24);
        } else {
            binding.appBarMain.connectionState.setImageResource(R.drawable.ic_baseline_cloud_off_24);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Service has disconnected.");
        isConnected = false;
    }
}
