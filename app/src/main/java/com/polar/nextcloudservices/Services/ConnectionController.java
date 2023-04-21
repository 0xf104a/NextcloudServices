package com.polar.nextcloudservices.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionController {
    private final ServiceSettings mServiceSettings;
    public ConnectionController(ServiceSettings settings){
        mServiceSettings = settings;
    }

    public boolean checkConnection(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            //We need to check only active network state
            final NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.isRoaming()) {
                        //Log.d(TAG, "Network is in roaming");
                        return mServiceSettings.isRoamingConnectionAllowed();
                    } else if (connectivity.isActiveNetworkMetered()) {
                        //Log.d(TAG, "Network is metered");
                        return mServiceSettings.isMeteredConnectionAllowed();
                    } else {
                        //Log.d(TAG, "Network is unmetered");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
