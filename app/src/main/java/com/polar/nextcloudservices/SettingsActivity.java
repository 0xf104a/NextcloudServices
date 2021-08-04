package com.polar.nextcloudservices;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.app.ActivityManager;
import android.widget.TabHost;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import nl.invissvenska.numberpickerpreference.NumberDialogPreference;
import nl.invissvenska.numberpickerpreference.NumberPickerPreferenceDialogFragment;


class NotificationServiceConnection implements ServiceConnection {
    private final String TAG = "SettingsActivity.NotificationServiceConnection";
    private final SettingsActivity.SettingsFragment settings;

    public NotificationServiceConnection(SettingsActivity.SettingsFragment _settings) {
        settings = _settings;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof NotificationService.Binder) {
            settings.setStatus(((NotificationService.Binder) service).getServiceStatus());
        } else {
            Log.wtf(TAG, "Bad Binder type passed!");
            throw new RuntimeException("Expected NotificationService.Binder");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Service has disconnected.");
    }
}

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsActivity";
    private final Handler mHandler = new Handler();
    private Timer mTimer = null;
    private StatusUpdateTimerTask mTask = null;

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

    class StatusUpdateTimerTask extends TimerTask {
        private final SettingsFragment settings;

        public StatusUpdateTimerTask(SettingsFragment _settings) {
            settings = _settings;
        }

        @Override
        public void run() {
            // run on another thread
            mHandler.post(() -> {
                Log.d(TAG, "Entered run in timer task");
                if (isNotificationServiceRunning()) {
                    updateNotificationServiceStatus(settings);
                } else {
                    (settings).setStatus("Disconnected: service is not running");
                }
            });
        }
    }

    private void startNotificationService() {
        ///--------
        Log.d(TAG, "startService: ENTERING");
        if (!isNotificationServiceRunning()) {
            Log.d(TAG, "Services is not running: creating intent to start it");
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
        } else {
            ServiceConnection connection = new NotificationServiceConnection(settings);
            bindService(new Intent(getApplicationContext(), NotificationService.class), connection, 0);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mTask != null){
            mTask.cancel();
            if(mTimer != null){
                mTimer.purge();
            }
        }
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
            mTask = new StatusUpdateTimerTask((SettingsFragment) settings);
            mTimer.scheduleAtFixedRate( mTask, 0, 5000);
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final String TAG = "SettingsActivity.SettingsFragment";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference myPref = (Preference) findPreference("oss_licenses");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), OSSLicensesActivity.class);
                    startActivity(intent);

                    return true;
                }
            });
        }

        public void setStatus(String _status) {
            EditTextPreference status = (EditTextPreference) findPreference("status");
            if (status == null) {
                Log.wtf(TAG, "Unexpected null result of findPreference");
                throw new RuntimeException("Expected EditTextPrefernce, but got null!");
            } else {
                status.setSummary(_status);
            }
        }


        @Override
        public void onDisplayPreferenceDialog(Preference preference) {

            if (preference instanceof NumberDialogPreference) {
                NumberDialogPreference dialogPreference = (NumberDialogPreference) preference;
                DialogFragment dialogFragment = NumberPickerPreferenceDialogFragment
                        .newInstance(
                                dialogPreference.getKey(),
                                dialogPreference.getMinValue(),
                                dialogPreference.getMaxValue(),
                                dialogPreference.getStepValue(),
                                dialogPreference.getUnitText()
                        );
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getParentFragmentManager(), TAG + ".NumberPicker");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }


}