package com.polar.nextcloudservices.Notification;

import com.polar.nextcloudservices.Notification.Processors.ActionsNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.basic.BasicNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.OpenBrowserProcessor;
import com.polar.nextcloudservices.Notification.Processors.spreed.NextcloudTalkProcessor;

public class NotificationConfig {
    public static final AbstractNotificationProcessor[] NOTIFICATION_PROCESSORS = {
            new BasicNotificationProcessor(),
            new NextcloudTalkProcessor(),
            new OpenBrowserProcessor(),
            new ActionsNotificationProcessor()
    };

    public static final String NOTIFICATION_CONTROLLER_EXT_DATA_KEY = "NotificationControllerExtData";
}
