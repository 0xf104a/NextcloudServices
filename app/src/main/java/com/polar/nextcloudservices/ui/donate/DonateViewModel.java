package com.polar.nextcloudservices.ui.donate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DonateViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DonateViewModel() {}

    public LiveData<String> getText() {
        return mText;
    }
}