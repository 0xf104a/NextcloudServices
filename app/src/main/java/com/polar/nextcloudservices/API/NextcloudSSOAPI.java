package com.polar.nextcloudservices.API;

/*
 * Implements API for accounts imported from nextcloud.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.graphics.drawable.IconCompat;

import com.nextcloud.android.sso.QueryParam;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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

    @Override
    public Bitmap getUserAvatar(NotificationService service, String userId) throws Exception {
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/index.php/avatar/"+userId+"/256 ", "/"))
                .build();
        InputStream stream = API.performNetworkRequest(request);
        return BitmapFactory.decodeStream(stream);
    }

    @Override
    public Bitmap getImagePreview(NotificationService service, String imageId) throws Exception {
        Collection<QueryParam> parameter = new LinkedList<>();
        parameter.add(new QueryParam("fileId", imageId));
        parameter.add(new QueryParam("x", "100"));
        parameter.add(new QueryParam("y", "100"));
        parameter.add(new QueryParam("a", "1"));
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod("GET")
                .setUrl(Uri.encode("/index.php/core/preview", "/"))
                .setParameter(parameter)
                .build();
        InputStream stream = API.performNetworkRequest(request);
        return BitmapFactory.decodeStream(stream);
    }

    @Override
    public void sendAction(NotificationService service, String link, String method) throws Exception {
        NextcloudRequest request = new NextcloudRequest.Builder().setMethod(method)
                .setUrl(Uri.encode(link, "/")).build();
        API.performNetworkRequestV2(request);
    }
}
