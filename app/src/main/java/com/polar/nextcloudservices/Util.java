package com.polar.nextcloudservices;

import android.content.pm.PackageManager;
import android.net.Uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.lang.reflect.Array;

public class Util {
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Clean-ups URL by removing domain and protocl if needed
     * Example: "https://cloud.example.com/query" -> "/query"
     * @param target Target URL to remove a domain from
     * @return String cleaned-up from protocol and domain
     */
    public static String cleanUpURLIfNeeded(String target){
        try {
            URI uri = new URI(target);
            return uri.getPath().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
