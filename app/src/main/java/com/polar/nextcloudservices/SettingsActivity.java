package com.polar.nextcloudservices;

import java.util.List;
import java.util.Timer;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.app.ActivityManager;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.polar.nextcloudservices.settings.NotificationServiceConnection;
import com.polar.nextcloudservices.settings.PreferenceUpdateTimerTask;
import com.polar.nextcloudservices.ui.settings.SettingsFragment;


public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsActivity";
    public final Handler mHandler = new Handler();
    private Timer mTimer = null;
    private PreferenceUpdateTimerTask mTask = null;
    private NotificationServiceConnection mServiceConnection = null;

    //Exit from activity when back arrow is pressed
    //https://stackoverflow.com/questions/34222591/navigate-back-from-settings-activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stopNotificationService() {
        if(isNotificationServiceRunning()) {
            Log.i(TAG, "Stopping service");
            Context context = getApplicationContext();
            context.stopService(new Intent(context, NotificationService.class));
        }
    }
    public void startNotificationService() {
        ///--------
        //Log.d(TAG, "startService: ENTERING");
        if (!isNotificationServiceRunning()&&getBoolPreference("enable_polling",true)) {
            Log.d(TAG, "Service is not running: creating intent to start it");
            startService(new Intent(getApplicationContext(), NotificationService.class));
        }
    }

    private boolean isNotificationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<?> services = manager.getRunningServices(Integer.MAX_VALUE);
        return (services.size() > 0);
    }

    private void updateNotificationServiceStatus(SettingsFragment settings) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        if (services.size() == 0) {
            Log.e(TAG, "Service is not running!");
            settings.setStatus("Disconnected: service is not running");
        } else if(mServiceConnection==null){
            mServiceConnection = new NotificationServiceConnection(settings);
            bindService(new Intent(getApplicationContext(), NotificationService.class), mServiceConnection, 0);
        } else {
            mServiceConnection.updateStatus();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged");
        Log.d(TAG, "key=" + key);
    }


    public String getPreference(String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(key, "");
    }

    private boolean getBoolPreference(String key, boolean fallback) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(key, fallback);
    }




    @Override
    public void onDestroy(){
        if (mTask != null){
            mTask.cancel();
            if(mTimer != null){
                mTimer.purge();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (mTask != null){
            mTask.cancel();
            if(mTimer != null){
                mTimer.purge();
            }
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, fragment)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        startNotificationService();
    }


    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager manager = getSupportFragmentManager();
        Fragment settings = manager.findFragmentById(R.id.settings);
        if (!(settings instanceof SettingsFragment)) {
            Log.wtf(TAG, "Programming error: settings fragment is not instance of SettingsFragment!");
            throw new RuntimeException("Programming error: settings fragment is not instance of SettingsFragment!");
        } else {
            if (mTimer == null) {
                mTimer = new Timer();
            }
            if (mTask != null){
                mTask.cancel();
                if(mTimer != null){
                    mTimer.purge();
                }
            }
            Log.d(TAG, "Starting timer");
            mTask = new PreferenceUpdateTimerTask(this, (SettingsFragment) settings);
            mTimer.scheduleAtFixedRate( mTask, 0, 5000);
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


}