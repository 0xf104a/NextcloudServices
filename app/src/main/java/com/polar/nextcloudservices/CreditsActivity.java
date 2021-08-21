package com.polar.nextcloudservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

public class CreditsActivity extends AppCompatActivity {

    private final String TAG = "CreditsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Credits");
        setContentView(R.layout.activity_credits);

        final String[] licenses = getResources().getStringArray(R.array.oss_libs);
        final String[] urls = getResources().getStringArray(R.array.oss_libs_links);

        ListView mListView = (ListView) findViewById(R.id.oss_licenses_list);
        if (mListView == null) {
            Log.wtf(TAG, "ListView is null!");
            throw new RuntimeException("ListView should not be null!");
        }
        ArrayAdapter<String> aAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, licenses);
        mListView.setAdapter(aAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[position]));
                startActivity(browserIntent);
            }
        });
    }
}