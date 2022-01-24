package com.polar.nextcloudservices;

import static com.polar.nextcloudservices.settings.SettingsUtils.getBoolPreference;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.ENABLE_SERVICE_PREFERENCE;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.SERVER_ADDRESS_PREFERENCE;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.SERVER_INSECURE_PREFERENCE;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.SERVER_LOGIN_PREFERENCE;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.SSO_ENABLED_PREFERENCE;
import static com.polar.nextcloudservices.ui.settings.SettingsFragment.SSO_NAME_PREFERENCE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;
import com.polar.nextcloudservices.Preferences.AppPreferences;
import com.polar.nextcloudservices.Preferences.PreferencesUtils;
import com.polar.nextcloudservices.databinding.ActivityMainBinding;
import com.polar.nextcloudservices.settings.NotificationServiceConnection;
import com.polar.nextcloudservices.ui.settings.SettingsFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NotificationServiceConnection mServiceConnection = null;

    private void updateFragment(){
        //Get Settings fragment, so we would be able to call onSSOEnabled
        FragmentManager mFragmentManger = this.getSupportFragmentManager();
        //FIXME: find id of settings fragment
        SettingsFragment mSettingsFragment = (SettingsFragment) mFragmentManger.findFragmentById(R.id.fragment_settings);
        if(mSettingsFragment == null){
            Log.wtf(TAG, "Failed to get SettingFragment");
            return ;
        }
        if(mSettingsFragment.isHidden()){
            //Do nothing: will be updated automatically
            return ;
        }
        mSettingsFragment.onSSOEnabled();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_mainview,
                R.id.nav_apps,
                R.id.nav_connection_settings,
                R.id.nav_credits,
                R.id.nav_contribute
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(navigationView, navController);

        AppPreferences.prepareExistingApps(this.getBaseContext());
        handleNotificationService();
        setupToolbars();
        // run after short while, so that the service can start up
        new Handler().postDelayed(this::updateNotificationServiceStatus, 2000L);

    }

    @Override
    public void onResume(){
        super.onResume();
        handleNotificationService();
        updateNotificationServiceStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();

        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, account -> {
                SingleAccountHelper.setCurrentAccount(context, account.name);
                PreferencesUtils.setSSOPreferences(context, account);
                stopNotificationService();
                startNotificationService();
                updateFragment();
            });
            updateProfileImage();
        } catch (AccountImportCancelledException | NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void stopService(){
        stopNotificationService();
        updateNotificationServiceStatus();
    }
    public void notifyServiceChange(){
        handleNotificationService();
        updateNotificationServiceStatus();
        try {
            updateProfileImage();
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
    }

    public void handleNotificationService() {
        if(getBoolPreference(this, ENABLE_SERVICE_PREFERENCE,true)){
            startNotificationService();
        } else {
            stopNotificationService();
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
        //Log.e(TAG, "startService: ENTERING");
        if (!isNotificationServiceRunning()) {
            Log.d(TAG, "Service is not running: creating intent to start it");
            startService(new Intent(getApplicationContext(), NotificationService.class));
        }
    }

    private boolean isNotificationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<?> services = manager.getRunningServices(Integer.MAX_VALUE);
        return (services.size() > 0);
    }

    private void updateNotificationServiceStatus() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        if (services.size() == 0) {
            Log.e(TAG, "Service is not running!");
        }  else {
            if(mServiceConnection==null){
                mServiceConnection = new NotificationServiceConnection();
                bindService(new Intent(getApplicationContext(), NotificationService.class), mServiceConnection, 0);
            }
            mServiceConnection.updateConnectionStateIndicator(binding);
        }
    }

    /**
     * Taken from package it.niedermann.owncloud.notes.main.MainActivity
     * https://github.com/stefan-niedermann/nextcloud-notes
     *
     */
    private void setupToolbars() {
        try {
            updateProfileImage();
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }

        binding.appBarMain.launchAccountSwitcher.setOnClickListener((v) -> openAccountChooser());
        binding.appBarMain.menuButton.setOnClickListener((v) -> binding.drawerLayout.openDrawer(GravityCompat.START));

        if(mServiceConnection != null){
            binding.appBarMain.connectionState.setOnClickListener((v) -> mServiceConnection.updateConnectionStateIndicator(binding));
        }
    }

    private void updateProfileImage() throws NextcloudFilesAppAccountNotFoundException, NoCurrentAccountSelectedException {
        if (!PreferencesUtils.getBoolPreference(this, SSO_ENABLED_PREFERENCE, false)) {
            binding.appBarMain.launchAccountSwitcher.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            return;
        }
        // If you stored the "default" account using setCurrentAccount(…) you can get the account by using the following line:
        SingleSignOnAccount ssoAccount = AccountImporter.getSingleSignOnAccount(this, PreferencesUtils.getPreference(this, SSO_NAME_PREFERENCE));

        GlideUrl url = new GlideUrl(ssoAccount.url+"/index.php/avatar/" + ssoAccount.userId + "/64");

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_account_circle_grey_24dp)
                .error(R.drawable.ic_account_circle_grey_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.appBarMain.launchAccountSwitcher);
    }

    public void openAccountChooser() {
        try {
            AccountImporter.pickNewAccount(this);
        } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
            UiExceptionManager.showDialogForException(getApplicationContext(), e);
        }
    }
}