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
import com.bumptech.glide.annotation.GlideModule;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.ContributorDetails;

import java.util.List;

public class CreditsAdapter extends ArrayAdapter<ContributorDetails> {
    Context context;
    List<ContributorDetails> details;

    public CreditsAdapter(@NonNull Context context, int resource, @NonNull List<ContributorDetails> objects) {
        super(context, resource, objects);
        this.context = context;
        this.details = objects;
    }

    @Nullable
    @Override
    public ContributorDetails getItem(int position) {
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
