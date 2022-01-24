package com.polar.nextcloudservices.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.polar.nextcloudservices.Notifications.NotificationUtils;
import com.polar.nextcloudservices.Preferences.AppPreferences;
import com.polar.nextcloudservices.R;

import java.util.ArrayList;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private ArrayList<String> mApps = new ArrayList<>();
    private Context mContext;

    public AppListAdapter(Context context, ArrayList<String> apps) {
        mContext = context;
        mApps = apps;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_row_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bindApp(mApps.get(position));
    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        Switch mEnabledSwitch;
        private Context mContext;

        public AppViewHolder(View itemView) {
            super(itemView);
            mEnabledSwitch = itemView.findViewById(R.id.filter_switch);
            mContext = itemView.getContext();
        }

        public void bindApp(String app) {
            mEnabledSwitch.setText(NotificationUtils.getTranslation(mContext, app));
            mEnabledSwitch.setChecked(AppPreferences.isAppEnabled(mContext, app));
            mEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> AppPreferences.setEnabled(mContext, app, isChecked));
        }
    }
}