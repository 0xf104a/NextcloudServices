package com.polar.nextcloudservices;

import android.content.pm.PackageManager;

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
     * @param domain Domain of a service
     * @param target Target URL to remove a domain from
     * @return String cleaned-up from protocol and domain
     */
    public static String cleanUpURLIfNeeded(String domain, String target){
        if(target.startsWith("http://")){
            target = target.replaceFirst("http://", "");
        }
        if(target.startsWith("https://")){
            target = target.replaceFirst("https://", "");
        }
        if(target.startsWith(domain)){
            target = target.replaceFirst(domain, "");
        }
        return target;
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
