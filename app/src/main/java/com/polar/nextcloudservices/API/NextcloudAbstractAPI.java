package com.polar.nextcloudservices.API;

import android.graphics.Bitmap;

import com.polar.nextcloudservices.Services.PollingService;
import com.polar.nextcloudservices.Services.Status.StatusCheckable;

import org.json.JSONObject;

import java.io.IOException;

/*
 * Nextcloud abstract API crates possibility to use different libraries for
 * polling for notifications. This is needed to use Nextcloud SSO library
 * since it does not give per-app key.
 * The inheritors of this interface should be passed to NotificationService.
 */
public interface NextcloudAbstractAPI extends StatusCheckable {
    JSONObject getNotifications(PollingService service);
    void removeNotification(int id);
    void sendTalkReply(String chatroom, String message) throws IOException;
    Bitmap getUserAvatar(String userId) throws Exception;
    Bitmap getImagePreview(String path) throws Exception;
    void sendAction(String link, String method) throws Exception;
}
