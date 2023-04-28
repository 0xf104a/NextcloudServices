package com.polar.nextcloudservices.Services.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.API.NextcloudHttpAPI;
import com.polar.nextcloudservices.API.NextcloudSSOAPI;

/**
 * Implements interface for accessing settings
 */
public class ServiceSettings {
    private static final String TAG = "Services.Settings.ServiceSettings";
    private final Context mContext;
    private NextcloudAbstractAPI mCachedAPI = null;

    public ServiceSettings(Context context){
        mContext = context;
    }

    private NextcloudAbstractAPI makeAPIFromSettings(){
        if (getBoolPreference("sso_enabled", false)) {
            final String name = getPreference("sso_name");
            final String server = getPreference("sso_server");
            final String type = getPreference("sso_type");
            final String token = getPreference("sso_token");
            final String userId = getPreference("sso_userid");
            final SingleSignOnAccount ssoAccount = new SingleSignOnAccount(name, userId, token, server, type);
            return new NextcloudSSOAPI(mContext, ssoAccount);
        } else {
            //We do not have an account -> use HTTP API
            Log.i(TAG, "No Nextcloud account was found.");
            return new NextcloudHttpAPI(this);
        }
    }

    public NextcloudAbstractAPI getAPIFromSettings(){
        if(mCachedAPI == null){
            mCachedAPI = makeAPIFromSettings();
        }
        return mCachedAPI;
    }

    public void onPreferencesChanged(){
        mCachedAPI = makeAPIFromSettings();
    }

    public String getPreference(String key) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getString(key, "<none>");
    }

    public Integer getIntPreference(String key, int i) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getInt(key, Integer.MIN_VALUE);
    }

    public boolean getBoolPreference(String key, boolean fallback) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getBoolean(key, fallback);
    }

    public boolean isMeteredConnectionAllowed(){
        return getBoolPreference(ServiceSettingConfig.ALLOW_METERED, false);
    }

    public boolean isRoamingConnectionAllowed(){
        return getBoolPreference(ServiceSettingConfig.ALLOW_ROAMING, false);
    }

    public boolean isRemoveOnDismissEnabled() {
        return getBoolPreference(ServiceSettingConfig.REMOVE_ON_DISMISS, false);
    }

    public int getPollingIntervalMs() {
        return getIntPreference(ServiceSettingConfig.POLLING_INTERVAL, 3) * 1000;
    }

    public String getUsername() {
        return getPreference(ServiceSettingConfig.USERNAME);
    }

    public String getPassword() {
        return getPreference(ServiceSettingConfig.PASSWORD);
    }

    public String getServer() {
        return getPreference(ServiceSettingConfig.SERVER);
    }

    public boolean getUseHttp(){
        return getBoolPreference(ServiceSettingConfig.USE_HTTP, false);
    }
}
