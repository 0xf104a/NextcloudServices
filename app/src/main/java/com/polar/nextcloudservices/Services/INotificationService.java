package com.polar.nextcloudservices.Services;

/**
 * Interface for communicating with notification service from side of UI
 */
public interface INotificationService {
     String getStatus();
     void onPreferencesChanged();
     void onAccountChanged();
}
