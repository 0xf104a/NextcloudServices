package com.polar.nextcloudservices.API;

import android.graphics.Bitmap;

import com.polar.nextcloudservices.Services.NotificationService;
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
    JSONObject getNotifications(NotificationService service);
    void removeNotification(NotificationService service, int id);
    void sendTalkReply(NotificationService service, String chatroom, String message) throws IOException;
    Bitmap getUserAvatar(NotificationService service, String userId) throws Exception;
    Bitmap getImagePreview(NotificationService service, String path) throws Exception;
    void sendAction(NotificationService service, String link, String method) throws Exception;
}
