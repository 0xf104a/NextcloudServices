package com.polar.nextcloudservices.Services.Status;

/*
 * Represents status of polling.
 * Stores boolean indicating whether service connecting and working
 * and a string reason why service has fault.
 */
public class Status {
    public String reason;
    public boolean isOk;

    public Status(boolean _isOk, String _reason){
        isOk = _isOk;
        reason = _reason;
    }

    public static Status Ok(){
        return new Status(true, null);
    }

    public static Status Failed(String reason){
        return new Status(false, reason);
    }

}
