package com.polar.nextcloudservices.ui.apps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polar.nextcloudservices.Adapters.AppListAdapter;
import com.polar.nextcloudservices.Preferences.AppPreferences;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.databinding.FragmentAppsBinding;

import java.util.ArrayList;


public class AppFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().toString();
    private FragmentAppsBinding binding;

    public static final String ENABLE_SERVICE_PREFERENCE = "enable_polling";


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        ArrayList<String> apps = AppPreferences.getApplist(this.getContext());
        RecyclerView mRecyclerView = root.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        AppListAdapter mAdapter = new AppListAdapter(this.getContext(), apps);
        mRecyclerView.setAdapter(mAdapter);



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}