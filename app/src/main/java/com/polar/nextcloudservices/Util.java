package com.polar.nextcloudservices;

import android.content.pm.PackageManager;
import android.util.Log;
import android.net.Uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.lang.reflect.Array;


public class Util {
    private static final String TAG = "Util";

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Clean-ups URL by removing domain and protocol if needed
     * according to wikipedia a uniform resource locator is composed of the following elements:
     * URI = scheme ":" ["//" authority] path ["?" query] ["#" fragment]
     * authority = [userinfo "@"] host [":" port]
     *
     * Example: "https://cloud.example.com/path?query#fragment" -> "/path?query#fragment"
     * @param target Target URL to remove everything in front of the path
     * @return String cleaned-up from protocol and domain
     */
   public static String cleanUpURLIfNeeded(String target){
        try {
            URI uri = new URI(target);
            String result = uri.getPath().toString();
            if(uri.getQuery() != null) {
                result = result + "?" + uri.getQuery().toString();
            }
            if(uri.getFragment() != null) {
                result = result + "#" + uri.getFragment().toString();
            }
            return result;
        } catch (URISyntaxException e) {
            Log.e(TAG, "error cleaning up target link.");
            e.printStackTrace();
            return null;
        }
   }

    public static <T>  boolean isInArray(T obj, T[] array){
        for (T t : array) {
            if (t == obj) {
                return true;
            }
        }
        return false;
    }
}
