package com.polar.nextcloudservices.Services;

public class NotificationServiceBinder extends android.os.Binder {
    private final INotificationService mNotificationService;
    public NotificationServiceBinder(INotificationService service){
        super();
        mNotificationService = service;
    }
    // Returns current status string of a service
    public String getServiceStatus() {
        return mNotificationService.getStatus();
    }

    // Runs re-check of preferences, can be called from activities
    public void onPreferencesChanged() {
        mNotificationService.onPreferencesChanged();
    }

    // Update API class when accounts state change
    public void onAccountChanged() {
        mNotificationService.onAccountChanged();
    }
}