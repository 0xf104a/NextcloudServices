package com.polar.nextcloudservices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.polar.nextcloudservices.Adapters.Credits_Adapter;

import java.util.ArrayList;
import java.util.List;

public class CreditsActivity extends AppCompatActivity {

    private final String TAG = "CreditsActivity";
    List<contributor_details> details = new ArrayList<>();
    String[] licenses;
    String[] urls;
    String[] owner_Name;
    String[] owner_github_Name;
    String[] owner_github_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Credits");
        setContentView(R.layout.activity_credits);
        licenses = getResources().getStringArray(R.array.oss_libs);
        urls = getResources().getStringArray(R.array.oss_libs_links);
        owner_Name = getResources().getStringArray(R.array.oss_libs_owner_name);
        owner_github_Name = getResources().getStringArray(R.array.oss_libs_owner_github_name);
        owner_github_image = getResources().getStringArray(R.array.oss_libs_owner_Img);

        details_add();
        ListView mListView = (ListView) findViewById(R.id.oss_licenses_list);
        if (mListView == null) {
            Log.wtf(TAG, "ListView is null!");
            throw new RuntimeException("ListView should not be null!");
        }
        Credits_Adapter aAdapter = new Credits_Adapter(this, R.layout.credits_contributer, details);
        mListView.setAdapter(aAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[position]));
                startActivity(browserIntent);
            }
        });
    }

    //    A function to add the contributor details in details(Arraylist) which is to be shown in credits activity
    public void details_add() {
        details.add(new contributor_details(owner_Name[0], licenses[0], owner_github_image[0], owner_github_Name[0]));
    }

}