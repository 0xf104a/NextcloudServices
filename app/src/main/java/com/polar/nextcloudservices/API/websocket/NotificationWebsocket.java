package com.polar.nextcloudservices.API.websocket;

import android.content.Context;
import android.util.Log;

import com.polar.nextcloudservices.Services.Status.Status;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class NotificationWebsocket extends WebSocketClient implements StatusCheckable {
    private final static String TAG = "NotificationWebsocket";
    private final String mUsername;
    private final String mPassword;
    private final INotificationWebsocketEventListener mNotificationListener;
    private String mStatus;
    private boolean isConnected;

    public NotificationWebsocket(URI serverUri, String username, String password,
                                 INotificationWebsocketEventListener notificationListener) {
        super(serverUri);
        mUsername = username;
        mPassword = password;
        mNotificationListener = notificationListener;
        isConnected = false;
        mStatus = "Disconnected";
    }

    /**
     * @param handshakedata The handshake of the websocket instance
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "Connected to websocket");
        send(mUsername);
        send(mPassword);
        send("listen notify_file_id");
        isConnected = true;
        mStatus = "Connected";
        mNotificationListener.onWebsocketConnected();
    }

    /**
     * @param message The UTF-8 decoded message that was received.
     */
    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Got message:" + message);
    }

    /**
     * @param code   The codes can be looked up here: {@link CloseFrame}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if(remote){
            Log.w(TAG, "Remote has disconnected, code=" + code + ", reason=" + reason);
            mStatus = "Remote disconnected";
        } else {
            Log.i(TAG, "We have disconnected, code=" + code + ", reason=" + reason);
            mStatus = "Disconnected";
        }
        isConnected = false;
        mNotificationListener.onWebsocketDisconnected(false);
    }

    /**
     * @param ex The exception causing this error
     */
    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "Error in websocket", ex);
        isConnected = false;
        mStatus = "Unexpected error in websocket connection";
        mNotificationListener.onWebsocketDisconnected(true);
    }

    /**
     * @param context context which may be used for obtaining app and device status
     * @return status information in form of Status class
     */
    @Override
    public Status getStatus(Context context) {
        if(isConnected){
            return Status.Ok();
        }else{
            return Status.Failed(mStatus);
        }
    }

    /**
     * Checks that websocket is connected
     * @return true if websocket is connected
     */
    public boolean getConnected(){
        return isConnected;
    }
}
