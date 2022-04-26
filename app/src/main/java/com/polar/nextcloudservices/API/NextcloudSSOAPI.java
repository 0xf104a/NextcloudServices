package com.polar.nextcloudservices.API;

/*
 * Implements API for accounts imported from nextcloud.
 */

import android.net.Uri;
import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NextcloudSSOAPI implements NextcloudAbstractAPI {
    private NextcloudAPI API;
    final private String TAG = "NextcloudSSOAPI";

    public NextcloudSSOAPI(NextcloudAPI mNextcloudAPI) {
        API = mNextcloudAPI;
    }

    @Override
    public JSONObject getNotifications(NotificationService service) {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<String>();
        values.add("application/json");
        header.put("Accept", values);

        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/ocs/v2.php/apps/notifications/api/v2/notifications", "/"))
                .setHeader(header)
                .build();
        StringBuilder buffer = new StringBuilder("");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(API.performNetworkRequest(request)));

            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
        } catch (Exception e) {
            service.status = "Disconnected: " + e.getLocalizedMessage();
            e.printStackTrace();
            return null;
        }

        try {
            JSONObject response = new JSONObject(buffer.toString());
            service.onPollFinished(response);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            service.status = "Disconnected: server has sent bad response: " + e.getLocalizedMessage();
            return null;
        }
    }

    @Override
    public void removeNotification(NotificationService service, int id) {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<String>();
        values.add("application/json");
        header.put("Accept", values);

        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("DELETE")
                .setUrl(Uri.encode("/ocs/v2.php/apps/notifications/api/v2/notifications/"+id, "/"))
                .setHeader(header)
                .build();
        try {
            API.performNetworkRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTalkReply(NotificationService service, String chatroom, String message) throws IOException {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<String>();
        values.add("application/json");
        header.put("Accept", values);
        header.put("Content-Type", values);

        //FIXME: build params in a better way
        final String params = "{\"message\": \"" + message + "\", \"chatroom\": \"" + chatroom + "\"}";

        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("POST")
                .setUrl(Uri.encode("/ocs/v2.php/apps/spreed/api/v1/chat/" + chatroom, "/"))
                .setHeader(header)
                .setRequestBody(params)
                .build();

        try {
            API.performNetworkRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
