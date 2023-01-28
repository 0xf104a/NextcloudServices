package com.polar.nextcloudservices.Notification;

public enum NotificationEvent {
    NOTIFICATION_EVENT_UNKNOWN(0),
    NOTIFICATION_EVENT_DELETE(1),
    NOTIFICATION_EVENT_FASTREPLY(2),
    NOTIFICATION_EVENT_CUSTOM_ACTION(3);
    public final int value;

    NotificationEvent(int _value){
        value = _value;
    }

    static public NotificationEvent fromInt(int code){
        switch (code){
            case 0:
                return NOTIFICATION_EVENT_UNKNOWN;
            case 1:
                return NOTIFICATION_EVENT_DELETE;
            case 2:
                return NOTIFICATION_EVENT_FASTREPLY;
            case 3:
                return NOTIFICATION_EVENT_CUSTOM_ACTION;
            default:
                throw new RuntimeException("Bad event code: " + code);
        }
    }
}
