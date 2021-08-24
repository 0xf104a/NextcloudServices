package com.polar.nextcloudservices.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.contributor_details;

import java.util.List;


public class Credits_Adapter extends ArrayAdapter<contributor_details> {
    Context context;
    List<contributor_details> details;

    public Credits_Adapter(@NonNull Context context, int resource, @NonNull List<contributor_details> objects) {
        super(context, resource, objects);
        this.context = context;
        this.details = objects;
    }

    @Nullable
    @Override
    public contributor_details getItem(int position) {
        return details.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.credits_contributer, parent, false);
        ImageView UserImage = convertView.findViewById(R.id.userImage);
        TextView userName = convertView.findViewById(R.id.userName);
        TextView contribution = convertView.findViewById(R.id.contribution);
        TextView githubName = convertView.findViewById(R.id.github_name);
        userName.setText(details.get(position).Name);
        contribution.setText(details.get(position).contribution);
        githubName.setText(details.get(position).github_name);
        Glide.with(context).load(details.get(position).imageUrl).placeholder(R.drawable.user).circleCrop().into(UserImage);
        return convertView;
    }
}
