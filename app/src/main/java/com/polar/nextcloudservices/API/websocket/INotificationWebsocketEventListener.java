package com.polar.nextcloudservices.API.websocket;

import com.polar.nextcloudservices.Services.INotificationListener;

public interface INotificationWebsocketEventListener extends INotificationListener {
    /**
     * Called whenever websocket is disconnected
     * @param isError whether disconnect resulted from error
     */
    void onWebsocketDisconnected(boolean isError);

    /**
     * Called whenever websocket connection is established
     */
    void onWebsocketConnected();
}
