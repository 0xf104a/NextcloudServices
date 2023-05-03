package com.polar.nextcloudservices.Services;

import org.json.JSONObject;

public interface PollUpdateListener {
    void onPollFinished(JSONObject response);
}
