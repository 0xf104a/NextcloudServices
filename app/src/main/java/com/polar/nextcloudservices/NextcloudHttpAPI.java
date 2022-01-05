package com.polar.nextcloudservices;

import static com.polar.nextcloudservices.Preferences.PreferencesUtils.NONE_RESULT;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    @Override
    public JSONObject getNotifications(NotificationService service) {
        try {
            String baseUrl = service.server;
            if(baseUrl.equals(NONE_RESULT)){
                throw new Exception("The serveradresss "+baseUrl+" is not valid!");
            }
            String prefix = "https://";
            if (service.useHttp) {
                prefix = "http://";
            }

            String endpoint = prefix + baseUrl + "/ocs/v2.php/apps/notifications/api/v2/notifications";
            Log.d(TAG, endpoint);
            URL url = new URL(endpoint);
            HttpURLConnection conn;
            if(service.useHttp) {
                conn = (HttpURLConnection) url.openConnection();
            }else{
                conn = (HttpsURLConnection) url.openConnection();
            }
            conn.setRequestProperty("Authorization", "Basic " + getAuth(service.username, service.password));
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("User-agent", UA);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(5000);
            //conn.setRequestMethod("GET");
            //Log.d(TAG, conn.getRequestProperties().toString());
            //conn.setDoOutput(true);
            conn.setDoInput(true);

            //OutputStream os = conn.getOutputStream();
            //os.close();
            String responseCode = Integer.toString(conn.getResponseCode());
            Log.d(TAG, "--> https://" + baseUrl + "/ocs/v2.php/apps/notifications/api/v2/notifications -- " + responseCode);

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
            //Todo: Translate message
            service.setStatus(NotificationService.STATE.DISCONNECTED, "Server has sent bad response: " + e.getLocalizedMessage());
            return null;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            //Todo: Translate message
            service.setStatus(NotificationService.STATE.DISCONNECTED, "File not found: check your credentials and Nextcloud instance.");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error while getting response");
            e.printStackTrace();
            //Todo: Translate message
            service.setStatus(NotificationService.STATE.DISCONNECTED, "I/O error: " + e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            //Todo: Translate message
            service.setStatus(NotificationService.STATE.DISCONNECTED, e.getLocalizedMessage());
            return null;
        }
    }
}
