package com.polar.nextcloudservices.Notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class NotificationControllerExtData implements Parcelable {
    private static final String TAG = "Notification.NotificationControllerExtData";
    private boolean id_override = false;
    private int notification_id_override = -1;

    protected NotificationControllerExtData(Parcel in) {
        id_override = in.readByte() != 0;
        notification_id_override = in.readInt();
    }

    public NotificationControllerExtData(){
        /* STUB */
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (id_override ? 1 : 0));
        dest.writeInt(notification_id_override);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotificationControllerExtData> CREATOR = new Creator<NotificationControllerExtData>() {
        @Override
        public NotificationControllerExtData createFromParcel(Parcel in) {
            return new NotificationControllerExtData(in);
        }

        @Override
        public NotificationControllerExtData[] newArray(int size) {
            return new NotificationControllerExtData[size];
        }
    };

    public boolean needOverrideId(){
        return id_override;
    }

    public int getNotificationId(){
        return notification_id_override;
    }

    public void setNotificationIdOverride(int id){
        if(id_override){
            Log.w(TAG, "Overriding notification id " + id + " which is already overriden");
        }
        id_override = true;
        notification_id_override = id;
    }

    public Bundle asBundle(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(NotificationConfig.NOTIFICATION_CONTROLLER_EXT_DATA_KEY, this);
        return bundle;
    }
}
