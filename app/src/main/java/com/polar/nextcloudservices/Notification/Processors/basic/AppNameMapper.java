package com.polar.nextcloudservices.Notification.Processors.basic;

import android.content.Context;

import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.Utils.CommonUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps Nextcloud app name to prettified name of app
 */
public class AppNameMapper {
    /**
     * Array of apps which have mapped names
     */
    public static final String[] MAPPABLE_APPS = {"spreed",
            "updatenotification", "twofactor_nextcloud_notification"};

    /**
     * A map mapping the mappable apps to resource id
     */
    public static final Map<String, Integer> APP_TO_RESID_MAPPING;
    static {
        // Put app mapping here
        Map<String, Integer> aMap = new HashMap<>();
        aMap.put("spreed", R.string.spreed_name);
        aMap.put("updatenotification", R.string.updatenotification_name);
        aMap.put("twofactor_nextcloud_notification", R.string.twofactor_nextcloud_notification_name);
        APP_TO_RESID_MAPPING = Collections.unmodifiableMap(aMap);
    }


    /**
     * @param appName Nextcloud app name
     * @return whether mapping to prettified name exists for provided app name
     */
    public static boolean isAppMappable(String appName){
        return CommonUtil.isInArray(appName, MAPPABLE_APPS);
    }

    /**
     * @param context Android context
     * @param appName Nextcloud app name
     * @return prettified app name from mapping
     * @throws RuntimeException if app has no prettified mapping
     */
    public static String getPrettifiedAppNameFromMapping(Context context, String appName){
        Integer result = APP_TO_RESID_MAPPING.get(appName);
        if(result == null){
            throw new RuntimeException("No entry for mapping app " + appName + " found");
        }
        return context.getString(result);
    }

    /**
     * Get prettified app name from mapping if exists, tries to prettify if not
     * @param context Android context
     * @param appName Nextcloud app name
     * @return prettified name of app
     */
    public static String getPrettifiedAppName(Context context, String appName){
        if(isAppMappable(appName)){
            return getPrettifiedAppNameFromMapping(context, appName);
        }
        String[] parts = appName.split("_");
        StringBuilder nice_name = new StringBuilder();
        for (String part : parts) {
            nice_name.append(part).append(" ");
        }
        String result = nice_name.toString();
        result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        return result;
    }

}
