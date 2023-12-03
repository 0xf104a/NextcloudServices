package com.polar.nextcloudservices.Services.Status;

import android.content.Context;

public interface StatusCheckable {
    /**
     * @param context context which may be used for obtaining app and device status
     * @return status information in form of Status class
     */
    Status getStatus(Context context);
}
