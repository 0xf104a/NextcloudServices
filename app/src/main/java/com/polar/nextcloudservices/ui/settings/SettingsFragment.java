package com.polar.nextcloudservices.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;
import com.polar.nextcloudservices.R;

import nl.invissvenska.numberpickerpreference.NumberDialogPreference;
import nl.invissvenska.numberpickerpreference.NumberPickerPreferenceDialogFragment;

public class SettingsFragment extends PreferenceFragmentCompat {


    private final String TAG = "SettingsActivity.SettingsFragment";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        setSSOPreferencesState();
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

    private void notifyService() {
        // todo: add mServiceConnection
        //activity.mServiceConnection.tellAccountChanged();
    }

    private void enableSSO(@NonNull SingleSignOnAccount account) {
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

    private void disableSSO() {
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

    private void setSSOPreferencesState() {
        Preference login_sso = findPreference("login_sso");
        if (login_sso == null) {
            Log.wtf(TAG, "login_sso preference is null!");
            throw new NullPointerException();
        }
        if (getBoolPreference("sso_enabled", false)) {
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

    public void setStatus(String _status) {
        EditTextPreference status = (EditTextPreference) findPreference("status");
        if (status == null) {
            Log.wtf(TAG, "Unexpected null result of findPreference");
            throw new RuntimeException("Expected EditTextPreference, but got null!");
        } else {
            status.setSummary(_status);
        }
    }
}