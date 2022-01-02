package com.polar.nextcloudservices.settings;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.polar.nextcloudservices.NotificationService;
import com.polar.nextcloudservices.ui.settings.SettingsFragment;

public class NotificationServiceConnection implements ServiceConnection {
    private final String TAG = "SettingsActivity.NotificationServiceConnection";
    private final SettingsFragment settings;
    private NotificationService.Binder mService;
    public boolean isConnected = false;

    public NotificationServiceConnection(SettingsFragment _settings) {
        settings = _settings;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof NotificationService.Binder) {
            mService = (NotificationService.Binder) service;
            settings.setStatus(((NotificationService.Binder) service).getServiceStatus());
            isConnected = true;
        } else {
            Log.wtf(TAG, "Bad Binder type passed!");
            throw new RuntimeException("Expected NotificationService.Binder");
        }
    }

    public void updateStatus() {
        if (!isConnected) {
            Log.w(TAG, "Service has already disconnected");
            settings.setStatus("Disconnected: service is not running");
        } else {
            settings.setStatus(mService.getServiceStatus());
        }
    }

    public void tellAccountChanged() {
        Log.d(TAG, "Telling service that account has cahnged");
        mService.onAccountChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Service has disconnected.");
        isConnected = false;
    }
}
