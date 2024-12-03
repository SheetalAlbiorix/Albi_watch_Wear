package com.wearos.viewmodel;

import android.app.Activity;

import androidx.credentials.CredentialManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wearos.model.WatchData;

import java.util.List;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<List<WatchData>> itemsLiveData = new MutableLiveData<>();

    private CredentialManager credentialManager;

    public void setCredentialManager(Activity activity) {
        credentialManager = CredentialManager.create(activity);
    }
}
