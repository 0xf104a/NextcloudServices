package com.polar.nextcloudservices.Services.Status;

import android.content.Context;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/*
 * Implements status-checking logic
 */
public class StatusController {
    private final HashMap<Integer, StatusCheckable> components;
    private final Context mContext;

    public StatusController(Context context){
        components = new HashMap<>();
        mContext = context;
    }

    public void addComponent(Integer componentId, StatusCheckable component){
        components.put(componentId, component);
    }

    public void removeComponent(Integer componentId){
        components.remove(componentId);
    }

    public Status check(){
        for(StatusCheckable component: components.values()){
            Status state = component.getStatus(mContext);
            if(!state.isOk){
                return Status.Failed(state.reason);
            }
        }
        return Status.Ok();
    }

    public String getStatusString(){
        Status status = check();
        if(status.isOk){
            return "Connected";
        } else {
            return status.reason;
        }
    }
}
