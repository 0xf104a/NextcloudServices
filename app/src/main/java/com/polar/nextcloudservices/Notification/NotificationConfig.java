package com.polar.nextcloudservices.Notification;

import com.polar.nextcloudservices.Notification.Processors.ActionsNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.BasicNotificationProcessor;
import com.polar.nextcloudservices.Notification.Processors.NextcloudTalkProcessor;
import com.polar.nextcloudservices.Notification.Processors.OpenBrowserProcessor;

public class NotificationConfig {
    public static final AbstractNotificationProcessor[] NOTIFICATION_PROCESSORS = {
            new BasicNotificationProcessor(),
            new NextcloudTalkProcessor(),
            new OpenBrowserProcessor(),
            new ActionsNotificationProcessor()
    };
}
