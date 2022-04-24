package com.polar.nextcloudservices;

import org.json.JSONObject;

/*
 * Nextcloud abstract API crates possibility to use different libraries for
 * polling for notifications. This is needed to use Nextcloud SSO library
 * since it does not give per-app key.
 * The inheritors of this interface should be passed to NotificationService.
 */
public interface NextcloudAbstractAPI {
    public JSONObject getNotifications(NotificationService service);
    public void removeNotification(NotificationService service, int id);
}
