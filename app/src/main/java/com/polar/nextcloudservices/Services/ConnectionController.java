package com.polar.nextcloudservices.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.polar.nextcloudservices.Services.Settings.ServiceSettings;
import com.polar.nextcloudservices.Services.Status.Status;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

/**
 * Checks connectivity to the network
 */
public class ConnectionController implements StatusCheckable {
    private final ServiceSettings mServiceSettings;
    private final static String TAG = "Services.ConnectionController";
    public ConnectionController(ServiceSettings settings){
        mServiceSettings = settings;
    }

    public boolean checkConnection(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    @Override
    public Status getStatus(Context context) {
        if(checkConnection(context)){
            return Status.Ok();
        }
        return Status.Failed("Disconnected: no suitable network found.");
    }

    public void addConnectionStatusListener(Context context, IConnectionStatusListener listener){
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                listener.onConnectionStatusChanged(checkConnection(context));
            }}, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }
}
