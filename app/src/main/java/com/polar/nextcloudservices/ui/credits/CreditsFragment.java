package com.polar.nextcloudservices.ui.credits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.polar.nextcloudservices.Adapters.CreditsAdapter;
import com.polar.nextcloudservices.ContributorDetails;
import com.polar.nextcloudservices.R;
import com.polar.nextcloudservices.databinding.FragmentCreditsBinding;

import java.util.ArrayList;
import java.util.List;

public class CreditsFragment extends Fragment {


    private final String TAG = "CreditsFragment";
    List<ContributorDetails> details = new ArrayList<>();
    String[] licenses;
    String[] urls;
    String[] owner_Name;
    String[] owner_github_Name;
    String[] owner_github_image;

    private CreditsViewModel creditsViewModel;
    private FragmentCreditsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        creditsViewModel =
                new ViewModelProvider(this).get(CreditsViewModel.class);

        binding = FragmentCreditsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        super.onCreate(savedInstanceState);
        //setTitle("Credits");
        //setContentView(R.layout.activity_credits);
        licenses = getResources().getStringArray(R.array.oss_libs);
        urls = getResources().getStringArray(R.array.oss_libs_links);
        owner_Name = getResources().getStringArray(R.array.oss_libs_owner_name);
        owner_github_Name = getResources().getStringArray(R.array.oss_libs_owner_github_name);
        owner_github_image = getResources().getStringArray(R.array.oss_libs_owner_Img);

        details_add();


        ListView mListView = binding.ossLicensesList;
        CreditsAdapter aAdapter = new CreditsAdapter(this.getContext(), R.layout.credits_contributer, details);
        mListView.setAdapter(aAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[position]));
                startActivity(browserIntent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //    A function to add the contributor details in details(Arraylist) which is to be shown in credits activity
    public void details_add() {
        for (int i = 0; i < owner_Name.length; ++i) {
            details.add(new ContributorDetails(owner_Name[i], licenses[i], owner_github_image[i], owner_github_Name[i]));
        }
    }
}