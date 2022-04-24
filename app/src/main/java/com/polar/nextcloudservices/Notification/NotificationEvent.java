package com.polar.nextcloudservices.Notification;

public enum NotificationEvent {
    NOTIFICATION_EVENT_UNKNOWN(0),
    NOTIFICATION_EVENT_DELETE(1);
    public final int value;

    NotificationEvent(int _value){
        value = _value;
    }

    static public NotificationEvent fromInt(int code){
        if(code == 0){
            return NOTIFICATION_EVENT_UNKNOWN;
        }else if(code == 1){
            return NOTIFICATION_EVENT_DELETE;
        } else {
            throw new RuntimeException("Bad event code" + code);
        }
    }
}
