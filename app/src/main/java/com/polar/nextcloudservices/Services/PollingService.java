package com.polar.nextcloudservices.Services;

import org.json.JSONObject;

public interface PollingService {
    void onPollFinished(JSONObject response);
}
