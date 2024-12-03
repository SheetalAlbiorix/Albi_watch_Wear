package com.wearos.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.wearos.databinding.ActivityMainBinding;
import com.wearos.di.DaggerAppComponent;
import com.wearos.model.AuthRepository;
import com.wearos.viewmodel.MainActivityViewModel;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GoogleFitDemo";
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    @Inject
    AuthRepository repository;
    ActivityMainBinding binding;
    MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        viewModel.setCredentialManager(MainActivity.this);
        // Dagger setup
        DaggerAppComponent.create().inject(this);

        // Set click listener for the authentication button
        binding.authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repository.signIn(MainActivity.this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == 235) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Recognitions Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Recognitions Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        repository.onActivityResult(MainActivity.this, requestCode, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, resultCode, data);
    }
}