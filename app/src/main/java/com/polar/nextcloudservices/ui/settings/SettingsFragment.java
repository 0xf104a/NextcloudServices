package com.polar.nextcloudservices.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.polar.nextcloudservices.MainActivity;
import com.polar.nextcloudservices.R;

import nl.invissvenska.numberpickerpreference.NumberDialogPreference;
import nl.invissvenska.numberpickerpreference.NumberPickerPreferenceDialogFragment;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    private final String TAG = this.getClass().toString();
    public static final String SSO_ENABLED_PREFERENCE = "sso_enabled";
    public static final String SSO_NAME_PREFERENCE = "sso_name";
    public static final String SSO_INUSE_PREFERENCE = "login_sso";
    public static final String SERVER_ADDRESS_PREFERENCE = "server";
    public static final String SERVER_PASSWORD_PREFERENCE = "password";
    public static final String SERVER_LOGIN_PREFERENCE = "login";
    public static final String SERVER_INSECURE_PREFERENCE = "insecure_connection";
    public static final String ENABLE_SERVICE_PREFERENCE = "enable_polling";



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setSSOPreferencesState();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SSO_ENABLED_PREFERENCE)) {
            setSSOPreferencesState();
        }
        if (key.equals(ENABLE_SERVICE_PREFERENCE)) {
            notifyParent();
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


    private boolean getBoolPreference(String key, boolean fallback) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(key, fallback);
    }

    private void notifyParent() {
        MainActivity main = ((MainActivity) this.getActivity());
        if(main != null){
            main.notifyServiceChange();
        }
    }


    private void disableSSO() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SSO_ENABLED_PREFERENCE, false);
        editor.apply();
        setSSOPreferencesState();
        notifyParent();
        MainActivity main = ((MainActivity) this.getActivity());
        if(main != null){
            main.stopService();
        }
    }

    private void enableSSO(){
        MainActivity main = ((MainActivity) this.getActivity());
        if (main == null){
            Log.wtf(TAG, "MainActivity is NULL!");
            return ;
        }
        main.openAccountChooser();
    }

    public void onSSOEnabled() {
        setSSOPreferencesState();
    }

    private void setSSOPreferencesState() {
        Preference login_sso = findPreference(SSO_INUSE_PREFERENCE);
        if (login_sso == null) {
            Log.wtf(TAG, "login_sso preference is null!");
            throw new NullPointerException();
        }
        if (getBoolPreference(SSO_ENABLED_PREFERENCE, false)) {
            //findPreference(SSO_INUSE_PREFERENCE).setEnabled(true);
            findPreference(SERVER_ADDRESS_PREFERENCE).setEnabled(false);
            findPreference(SERVER_PASSWORD_PREFERENCE).setEnabled(false);
            findPreference(SERVER_LOGIN_PREFERENCE).setEnabled(false);
            findPreference(SERVER_INSECURE_PREFERENCE).setEnabled(false);
            findPreference(SSO_INUSE_PREFERENCE).setTitle(R.string.pref_sso_logout_title);
            findPreference(SSO_INUSE_PREFERENCE).setSummary(R.string.pref_sso_logout_description);

            login_sso.setOnPreferenceClickListener(preference -> {
                Log.d(TAG, "Disabling SSO");
                disableSSO();
                findPreference(SSO_INUSE_PREFERENCE).setTitle(R.string.pref_sso_login_title);
                findPreference(SSO_INUSE_PREFERENCE).setSummary(R.string.pref_sso_login_description);
                return true;
            });
        } else {
            findPreference(SERVER_ADDRESS_PREFERENCE).setEnabled(true);
            findPreference(SERVER_PASSWORD_PREFERENCE).setEnabled(true);
            findPreference(SERVER_LOGIN_PREFERENCE).setEnabled(true);
            findPreference(SERVER_INSECURE_PREFERENCE).setEnabled(true);
            findPreference(SSO_INUSE_PREFERENCE).setTitle(R.string.pref_sso_login_title);
            findPreference(SSO_INUSE_PREFERENCE).setSummary(R.string.pref_sso_login_description);
            //findPreference(SSO_INUSE_PREFERENCE).setEnabled(true);

            //We need to re-enable SSO_INUSE, since it allows to login/log out from app
            login_sso.setOnPreferenceClickListener(preference -> {
                Log.d(TAG, "Enabling SSO");
                enableSSO();
                return true;
            });
        }
    }
}