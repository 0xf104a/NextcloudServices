package com.polar.nextcloudservices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.polar.nextcloudservices.Adapters.CreditsAdapter;

import java.util.ArrayList;
import java.util.List;

public class CreditsActivity extends AppCompatActivity {

    private final String TAG = "CreditsActivity";
    List<ContributorDetails> details = new ArrayList<>();
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
        CreditsAdapter aAdapter = new CreditsAdapter(this, R.layout.credits_contributer, details);
        mListView.setAdapter(aAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomTabsIntent browserIntent = new CustomTabsIntent.Builder()
                        .setUrlBarHidingEnabled(true)
                        .setShowTitle(false)
                        .setStartAnimations(parent.getContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                        .setExitAnimations(parent.getContext(), android.R.anim.fade_in, android.R.anim.fade_out)
                        .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                        .build();
                browserIntent.launchUrl(parent.getContext(), Uri.parse(urls[position]));
            }
        });
    }

    //    A function to add the contributor details in details(Arraylist) which is to be shown in credits activity
    public void details_add() {
        for (int i = 0; i < owner_Name.length; ++i) {
            details.add(new ContributorDetails(owner_Name[i], licenses[i], owner_github_image[i], owner_github_Name[i]));
        }
    }

}