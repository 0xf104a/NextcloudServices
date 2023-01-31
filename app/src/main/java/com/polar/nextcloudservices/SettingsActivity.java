package com.polar.nextcloudservices;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.app.ActivityManager;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import nl.invissvenska.numberpickerpreference.NumberDialogPreference;
import nl.invissvenska.numberpickerpreference.NumberPickerPreferenceDialogFragment;


class NotificationServiceConnection implements ServiceConnection {
    private final String TAG = "SettingsActivity.NotificationServiceConnection";
    private final SettingsActivity.SettingsFragment settings;
    private NotificationService.Binder mService;
    public boolean isConnected = false;

    public NotificationServiceConnection(SettingsActivity.SettingsFragment _settings) {
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

    public void updateStatus(){
        if(!isConnected){
            Log.w(TAG, "Service has already disconnected");
            settings.setStatus("Disconnected: service is not running");
        } else {
            settings.setStatus(mService.getServiceStatus());
        }
    }

    public void tellAccountChanged(){
        Log.d(TAG, "Telling service that account has cahnged");
        mService.onAccountChanged();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Service has disconnected.");
        isConnected = false;
    }
}

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsActivity";
    private final Handler mHandler = new Handler();
    private Timer mTimer = null;
    private PreferenceUpdateTimerTask mTask = null;
    private NotificationServiceConnection mServiceConnection = null;
    private static final int NOTIFICATION_PERMISSION_CODE = 1;

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

    class PreferenceUpdateTimerTask extends TimerTask {
        private final SettingsFragment settings;
        public PreferenceUpdateTimerTask(SettingsFragment _settings) {
            settings = _settings;
        }

        @Override
        public void run() {
            // run on another thread
            mHandler.post(() -> {
                //Log.d(TAG, "Entered run in preference updater timer task");
                if(!getBoolPreference("enable_polling", true)){
                    stopNotificationService();
                } else if(!isNotificationServiceRunning()) {
                    startNotificationService();
                }
                if (isNotificationServiceRunning()) {
                    updateNotificationServiceStatus(settings);
                } else {
                    (settings).setStatus("Disconnected: service is not running");
                }
            });
        }
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
            Log.d(TAG,"savedInstanceState is null.");
            SettingsFragment fragment = new SettingsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, fragment)
                    .commit();
        } else {
            Log.d(TAG, "savedInstanceState is not null.");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        requestNotificationPermission();
        startNotificationService();
    }


    public void requestNotificationPermission() {
        if(Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "post notification permission is granted.");
                return;
            }
            Log.d(TAG, "post notification permission is not granted yet, so will request it now");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (!(grantResults.length > 0) || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "User denied notification permission");
                Toast.makeText(this, "Permission to send notifications is not granted. Use Settings app to grant it manually", Toast.LENGTH_LONG).show();
            }
        }
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
            mTask = new PreferenceUpdateTimerTask((SettingsFragment) settings);
            mTimer.scheduleAtFixedRate( mTask, 0, 5000);
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final String TAG = "SettingsActivity.SettingsFragment";
        //private DialogFragment current_dialog_fragment = null;

        private boolean getBoolPreference(String key, boolean fallback) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            return sharedPreferences.getBoolean(key, fallback);
        }

        private void notifyService(){
            SettingsActivity activity = (SettingsActivity) getActivity();
            if(activity == null){
                Log.wtf(TAG, "Activity can not be null!");
                throw new NullPointerException();
            }
            activity.mServiceConnection.tellAccountChanged();
        }

        private void enableSSO(@NonNull SingleSignOnAccount account){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sso_enabled", true);
            editor.putString("sso_name", account.name);
            editor.putString("sso_server", account.url);
            editor.putString("sso_type", account.type);
            editor.putString("sso_token", account.token);
            editor.putString("sso_userid", account.userId);
            editor.apply();
            setSSOPreferencesState();
            notifyService();
        }

        private void disableSSO(){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sso_enabled", false);
            editor.apply();
            setSSOPreferencesState();
            notifyService();
        }

        private void openAccountChooser() {
            try {
                AccountImporter.pickNewAccount(this);
            } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
                UiExceptionManager.showDialogForException(getContext(), e);
            }
        }


        private void setSSOPreferencesState(){
            Preference login_sso = findPreference("login_sso");
            if(login_sso == null){
                Log.wtf(TAG, "login_sso preference is null!");
                throw new NullPointerException();
            }
            if(getBoolPreference("sso_enabled",false)){
                findPreference("server").setEnabled(false);
                findPreference("password").setEnabled(false);
                findPreference("login").setEnabled(false);
                findPreference("insecure_connection").setEnabled(false);
                login_sso.setSummary("Stop using Nextcloud app for authentication");
                login_sso.setTitle("Log out from Nexcloud");
                login_sso.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "Disabling SSO");
                        disableSSO();
                        return true;
                    }
                });
            } else {
                findPreference("server").setEnabled(true);
                findPreference("password").setEnabled(true);
                findPreference("login").setEnabled(true);
                findPreference("insecure_connection").setEnabled(true);
                login_sso.setSummary("Use on-device Nextcloud account");
                login_sso.setTitle("Log in via Nextcloud app");
                login_sso.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "Opening account chooser");
                        openAccountChooser();
                        return true;
                    }
                });
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference oss_licenses = (Preference) findPreference("credits");
            oss_licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), CreditsActivity.class);
                    startActivity(intent);

                    return true;
                }
            });

            Preference donate = (Preference) findPreference("donate");
            donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CustomTabsIntent browserIntent = new CustomTabsIntent.Builder()
                            .setUrlBarHidingEnabled(true)
                            .setShowTitle(false)
                            .setStartAnimations(getContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                            .setExitAnimations(getContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                            .build();
                    browserIntent.launchUrl(getContext(), Uri.parse("https://liberapay.com/Andrewerr/donate"));
                    Toast.makeText(getContext(),"Thank you!❤️", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            /*
            Preference login_sso = (Preference) findPreference("login_sso");
            login_sso.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "Opening account chooser");
                    openAccountChooser();
                    return true;
                }
            });
             */

            EditTextPreference passwordPreference = findPreference("password");

            if (passwordPreference != null) {
                passwordPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            }
                        });
            }

            setSSOPreferencesState();
        }

        public void setStatus(String _status) {
            EditTextPreference status = (EditTextPreference) findPreference("status");
            if (status == null) {
                Log.wtf(TAG, "Unexpected null result of findPreference");
                throw new RuntimeException("Expected EditTextPreference, but got null!");
            } else {
                status.setSummary(_status);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            try {
                AccountImporter.onActivityResult(requestCode, resultCode, data, this, new AccountImporter.IAccountAccessGranted() {

                    @Override
                    public void accountAccessGranted(SingleSignOnAccount singleSignOnAccount) {
                        enableSSO(singleSignOnAccount);
                        Log.i(TAG, "Succesfully imported account");
                    }

                    NextcloudAPI.ApiConnectedListener callback = new NextcloudAPI.ApiConnectedListener() {
                        @Override
                        public void onConnected() {
                            // ignore this one… see 5)
                        }

                        @Override
                        public void onError(Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(getContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    };

                });
            } catch (AccountImportCancelledException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            Log.d(TAG, "Succesfully got Nextcloud permissions");
            AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            Log.d(TAG, "Displaying preference dialog.");
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
                Log.d(TAG, "Showing dialog fragment.");
                //current_dialog_fragment = dialogFragment;
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause called");
            /*if(current_dialog_fragment != null){
                Log.d(TAG, "Fragment has dialog opened.");
                current_dialog_fragment.onPause();
            }*/
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "onResume called");
        }

    }


}