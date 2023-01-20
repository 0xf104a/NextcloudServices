package com.polar.nextcloudservices.API;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.core.graphics.drawable.IconCompat;

import com.polar.nextcloudservices.API.NextcloudAbstractAPI;
import com.polar.nextcloudservices.BuildConfig;
import com.polar.nextcloudservices.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class NextcloudHttpAPI implements NextcloudAbstractAPI {
    private final String TAG = "NextcloudHttpAPI";
    private final String UA = "NextcloudServices/" + BuildConfig.VERSION_NAME;

    private String getAuth(String user, String password) {
        //Log.d("NotificationService.PollTask",user+":"+password);
        return Base64.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.DEFAULT).toString();
    }

    private HttpURLConnection request(NotificationService service, String path, String method,
                                      Boolean setAccept) throws IOException {
        String baseUrl = service.server;
        String prefix = "https://";
        if (service.useHttp) {
            prefix = "http://";
        }
        String endpoint = prefix + baseUrl + path;
        //Log.d(TAG, endpoint);
        URL url = new URL(endpoint);
        HttpURLConnection conn;
        if (service.useHttp) {
            conn = (HttpURLConnection) url.openConnection();
        } else {
            conn = (HttpsURLConnection) url.openConnection();
        }
        conn.setRequestProperty("Authorization", "Basic " + getAuth(service.username, service.password));
        conn.setRequestProperty("Host", url.getHost());
        conn.setRequestProperty("User-agent", UA);
        conn.setRequestProperty("OCS-APIRequest", "true");
        if (setAccept) {
            conn.setRequestProperty("Accept", "application/json");
        }
        conn.setRequestMethod(method);
        conn.setReadTimeout(60000);
        conn.setConnectTimeout(5000);
        return conn;
    }

    @Override
    public void removeNotification(NotificationService service, int id) {
        try {
            String prefix = "https://";
            if (service.useHttp) {
                prefix = "http://";
            }
            HttpURLConnection conn = request(service, "/ocs/v2.php/apps/notifications/api/v2/notifications/" + id,
                    "DELETE", false);
            String responseCode = Integer.toString(conn.getResponseCode());
            Log.d(TAG, "--> DELETE " + prefix + service.server + "/ocs/v2.php/apps/notifications/api/v2/notifications/" + id + " -- " + responseCode);
        } catch (IOException e) {
            Log.e(TAG, "Failed to DELETE notification: " + e.getLocalizedMessage());
            Log.d(TAG, "Exception was: " + e.toString());
        }
    }

    @Override
    public void sendTalkReply(NotificationService service, String chatroom, String message) throws IOException {
        //FIXME: Refactor to separate post function
        String baseUrl = service.server;
        String prefix = "https://";
        if (service.useHttp) {
            prefix = "http://";
        }
        String endpoint = prefix + baseUrl + "/ocs/v2.php/apps/spreed/api/v1/chat/" + chatroom;
        URL url = new URL(endpoint);
        HttpURLConnection conn;
        if (service.useHttp) {
            conn = (HttpURLConnection) url.openConnection();
        } else {
            conn = (HttpsURLConnection) url.openConnection();
        }
        conn.setRequestProperty("Authorization", "Basic " + getAuth(service.username, service.password));
        conn.setRequestProperty("Host", url.getHost());
        conn.setRequestProperty("User-agent", UA);
        conn.setRequestProperty("OCS-APIRequest", "true");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);

        //FIXME: create separate params generator
        final String params = "{\"message\": \"" + message + "\", \"chatroom\": \"" + chatroom + "\"}";
        OutputStream os = conn.getOutputStream();
        os.write(params.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        Log.d(TAG, "--> POST " + endpoint + " -- " + code);

    }

    public Bitmap getUserAvatar(NotificationService service, String userId) throws IOException {
        HttpURLConnection connection = request(service, "/index.php/avatar/"+userId+"/256",
                "GET", false);
        connection.setDoInput(true);
        return BitmapFactory.decodeStream(connection.getInputStream());
    }

    @Override
    public Bitmap getImagePreview(NotificationService service, String imageId) throws Exception {
        // FIXME: This code is untested!
        /*
        HttpURLConnection connection = request(service, "/index.php/core/preview?fileId="+imageId+"&x=100&y=100&a=1",
                "GET", false);
        connection.setRequestProperty("Accept", "image/*");
        connection.setDoInput(true);

        int responseCode = connection.getResponseCode();
        Log.d(TAG, "--> GET image preview --> " + responseCode);

        return BitmapFactory.decodeStream(connection.getInputStream());
         */
        return null;
    }

    @Override
    public JSONObject getNotifications(NotificationService service) {
        try {
            HttpURLConnection conn = request(service, "/ocs/v2.php/apps/notifications/api/v2/notifications",
            "GET", true);
            conn.setDoInput(true);

            String responseCode = Integer.toString(conn.getResponseCode());
            Log.d(TAG, "--> GET https://" + service.server + "/ocs/v2.php/apps/notifications/api/v2/notifications -- " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder("");
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            //Log.d(TAG, buffer.toString());
            JSONObject response = new JSONObject(buffer.toString());
            service.onPollFinished(response);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            service.status = "Disconnected: server has sent bad response: " + e.getLocalizedMessage();
            return null;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            service.status = "Disconnected: File not found: check your credentials and Nextcloud instance.";
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error while getting response");
            e.printStackTrace();
            service.status = "Disconnected: I/O error: " + e.getLocalizedMessage();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            service.status = "Disconnected: " + e.getLocalizedMessage();
            return null;
        }
    }
}
