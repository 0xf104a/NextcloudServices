package com.polar.nextcloudservices.API;

import android.graphics.Bitmap;

import com.polar.nextcloudservices.API.websocket.NotificationWebsocket;
import com.polar.nextcloudservices.API.websocket.NotificationWebsocketEventListener;
import com.polar.nextcloudservices.Services.NotificationListener;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONObject;

import java.io.IOException;

/*
 * Nextcloud abstract API creates possibility to use different libraries for
 * polling for notifications. This is needed to use Nextcloud SSO library
 * since it does not give per-app key.
 * The inheritors of this interface should be passed to NotificationService.
 */
public interface NextcloudAbstractAPI extends StatusCheckable {
    /**
     * Gets all notifications from server
     * @param service PollUpdateListener which handles notifications
     * @return notifications response as a JSONObject
     */
    JSONObject getNotifications(NotificationListener service);

    /**
     * Removes notification from server
     * @param id id of notification to remove
     */
    void removeNotification(int id);

    /**
     * Sends reply to talk chatroom
     * @param chatroom id of a chat
     * @param message message to send
     * @throws IOException in case of network error
     */
    void sendTalkReply(String chatroom, String message) throws IOException;

    /**
     * Get user avatar
     * @param userId username to get avatar of
     * @return avatar bitmap
     * @throws Exception in case of any errors
     */
    Bitmap getUserAvatar(String userId) throws Exception;

    /**
     * Gets image preview from server
     * @param path path to image
     * @return bitmap received from server
     * @throws Exception in case of any errors
     */
    Bitmap getImagePreview(String path) throws Exception;

    /**
     * Executes action which is inside of notifications
     * @param link Link to action
     * @param method method which should be used for querying link
     * @throws Exception in case of any errors
     */
    void sendAction(String link, String method) throws Exception;

    /**
     * @doc Checks new notifications without querying all of them directly
     * @return true if there is new notifications on server
     * @throws Exception in case of any error
     */
    boolean checkNewNotifications() throws Exception;

    /**
     * @return WebsocketClient instance which holds pre-authorized connection
     * @throws Exception in case of any unhandlable error
     * @doc Gets websocket client which is authorized and receives notification updates
     */
    NotificationWebsocket getNotificationsWebsocket(NotificationWebsocketEventListener listener) throws Exception;

}
