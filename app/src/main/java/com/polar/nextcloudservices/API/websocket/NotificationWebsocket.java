package com.polar.nextcloudservices.API.websocket;

import android.util.Log;

import com.polar.nextcloudservices.Services.NotificationListener;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class NotificationWebsocket extends WebSocketClient {
    private final static String TAG = "NotificationWebsocket";
    private final String mUsername;
    private final String mPassword;
    private final NotificationListener mNotificationListener;

    public NotificationWebsocket(URI serverUri, String username, String password,
                                 NotificationListener notificationListener) {
        super(serverUri);
        mUsername = username;
        mPassword = password;
        mNotificationListener = notificationListener;
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
        } else {
            Log.i(TAG, "We have disconnected, code=" + code + ", reason=" + reason);
        }
    }

    /**
     * @param ex The exception causing this error
     */
    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "Error in websocket", ex);
    }
}
