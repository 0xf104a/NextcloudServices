package com.polar.nextcloudservices.API;

/*
 * Implements API for accounts imported from nextcloud.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.QueryParam;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.api.Response;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.polar.nextcloudservices.Services.PollingService;
import com.polar.nextcloudservices.Services.Status.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NextcloudSSOAPI implements NextcloudAbstractAPI {
    final private NextcloudAPI API;
    final private static String TAG = "NextcloudSSOAPI";
    private boolean lastPollSuccessful = false;
    private String mStatusString = "Updating settings";

    public NextcloudSSOAPI(Context context, SingleSignOnAccount ssoAccount) {
        // ignore this one… see 5)
        NextcloudAPI.ApiConnectedListener apiCallback = new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {
                // ignore this one… see 5)
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        API = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), apiCallback);
    }

    @Override
    public JSONObject getNotifications(PollingService service) {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<>();
        values.add("application/json");
        header.put("Accept", values);

        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/ocs/v2.php/apps/notifications/api/v2/notifications", "/"))
                .setHeader(header)
                .build();
        StringBuilder buffer = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(API.performNetworkRequestV2(request).getBody()));

            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
        } catch (Exception e) {
            mStatusString = "Disconnected: " + e.getLocalizedMessage();
            lastPollSuccessful = false;
            e.printStackTrace();
            return null;
        }

        try {
            JSONObject response = new JSONObject(buffer.toString());
            service.onPollFinished(response);
            Log.d(TAG, "Setting lastPollSuccessful as true");
            lastPollSuccessful = true;
            return response;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            mStatusString = "Disconnected: server has sent bad response: " + e.getLocalizedMessage();
            return null;
        }
    }

    @Override
    public void removeNotification(int id) {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<>();
        values.add("application/json");
        header.put("Accept", values);

        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("DELETE")
                .setUrl(Uri.encode("/ocs/v2.php/apps/notifications/api/v2/notifications/"+id, "/"))
                .setHeader(header)
                .build();
        try {
            API.performNetworkRequestV2(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTalkReply(String chatroom, String message) {
        Map<String, List<String>> header = new HashMap<>();
        LinkedList<String> values = new LinkedList<>();
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
            API.performNetworkRequestV2(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap getUserAvatar(String userId) throws Exception {
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/index.php/avatar/"+userId+"/256 ", "/"))
                .build();
        InputStream stream = API.performNetworkRequestV2(request).getBody();
        return BitmapFactory.decodeStream(stream);
    }

    @Override
    public Bitmap getImagePreview(String imageId) throws Exception {
        Collection<QueryParam> parameter = new LinkedList<>();
        parameter.add(new QueryParam("fileId", imageId));
        parameter.add(new QueryParam("x", "100"));
        parameter.add(new QueryParam("y", "100"));
        parameter.add(new QueryParam("a", "1"));
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/index.php/core/preview", "/"))
                .setParameter(parameter)
                .build();
        InputStream stream = API.performNetworkRequestV2(request).getBody();
        return BitmapFactory.decodeStream(stream);
    }

    @Override
    public void sendAction(String link, String method) throws Exception {
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod(method)
                .setUrl(Uri.encode(link, "/")).build();
        API.performNetworkRequestV2(request);
    }

    @Override
    public Status getStatus(Context context) {
        if(lastPollSuccessful){
            Log.d(TAG, "Last poll is successful");
            return Status.Ok();
        }
        return Status.Failed(mStatusString);
    }
}
