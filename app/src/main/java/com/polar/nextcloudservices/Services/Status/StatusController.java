package com.polar.nextcloudservices.Services.Status;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/*
 * Implements status-checking logic
 */
public class StatusController {
    private static final String TAG = "Services.Status.StatusController";
    private final HashMap<Integer, StatusCheckable> components;
    private final HashMap<Integer, Integer> components_priority_mapping;
    private final Context mContext;

    public StatusController(Context context){
        components = new HashMap<>();
        mContext = context;
        components_priority_mapping = new HashMap<>();
    }

    public void addComponent(@NonNull Integer componentId, @NonNull StatusCheckable component,
                             @NonNull Integer priority){
        components.put(componentId, component);
        components_priority_mapping.put(componentId, priority);
    }

    public void removeComponent(Integer componentId){
        components.remove(componentId);
        components_priority_mapping.remove(componentId);
    }

    public Status check(){
        Integer maxPriority = Integer.MIN_VALUE;
        Status status = Status.Ok();
        for(Integer componentId: components.keySet()){
            StatusCheckable component = components.get(componentId);
            if(component == null){
                Log.e(TAG, "Can not get status for component with id: " + componentId);
                Log.e(TAG, "Component is null, skipping it");
                continue;
            }
            Status state = component.getStatus(mContext);
            Integer priority = components_priority_mapping.getOrDefault(componentId,
                    Integer.MIN_VALUE);
            if(!state.isOk){
                Log.d(TAG, "Got status: " + state.reason);
                Log.d(TAG, "Component id: " + componentId);
                Log.d(TAG, "Component prio: " + priority);
                if(priority >= maxPriority){
                    status = state;
                    maxPriority = priority;
                }
            }
        }
        return status;
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
