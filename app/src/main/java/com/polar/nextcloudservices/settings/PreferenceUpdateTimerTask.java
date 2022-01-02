package com.polar.nextcloudservices.settings;

import com.polar.nextcloudservices.SettingsActivity;
import com.polar.nextcloudservices.ui.settings.SettingsFragment;

import java.util.TimerTask;

public class PreferenceUpdateTimerTask extends TimerTask {
    private final SettingsActivity settingsActivity;
    private final SettingsFragment settings;

    public PreferenceUpdateTimerTask(SettingsActivity settingsActivity, SettingsFragment _settings) {
        this.settingsActivity = settingsActivity;
        settings = _settings;
    }

    @Override
    public void run() {
        // run on another thread
       /* settingsActivity.mHandler.post(() -> {
            //Log.d(TAG, "Entered run in preference updater timer task");
            if (!settingsActivity.getBoolPreference("enable_polling", true)) {
                settingsActivity.stopNotificationService();
            } else if (!settingsActivity.isNotificationServiceRunning()) {
                settingsActivity.startNotificationService();
            }
            if (settingsActivity.isNotificationServiceRunning()) {
                settingsActivity.updateNotificationServiceStatus(settings);
            } else {
                (settings).setStatus("Disconnected: service is not running");
            }
        });
        */
    }
}
