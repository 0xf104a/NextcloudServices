package com.polar.nextcloudservices.Notification;

public enum NotificationEvent {
    NOTIFICATION_EVENT_UNKNOWN(0),
    NOTIFICATION_EVENT_DELETE(1),
    NOTIFICATION_EVENT_FASTREPLY(2);
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
            default:
                throw new RuntimeException("Bad event code: " + code);
        }
    }
}
