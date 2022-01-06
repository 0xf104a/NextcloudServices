package com.polar.nextcloudservices.ui.contribute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.polar.nextcloudservices.databinding.FragmentContributeBinding;

public class ContributeFragment extends Fragment {

    private FragmentContributeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContributeBinding.inflate(inflater, container, false);

        binding.buttonGitgub.setOnClickListener((View v) -> {
            openUrl("https://github.com/Andrewerr/NextcloudServices");
        });
        binding.buttonTranslations.setOnClickListener((View v) -> {
            openUrl("");
        });
        binding.buttonDonate.setOnClickListener((View v) -> {
            openUrl("");
        });
        binding.labelLicense.setOnClickListener((View v) -> {
            openUrl("https://github.com/Andrewerr/NextcloudServices/blob/main/LICENSE");
        });
        
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openUrl(String url){
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}