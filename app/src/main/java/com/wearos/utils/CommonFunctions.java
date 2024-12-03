package com.wearos.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


// Function to check and request permission
public class CommonFunctions {
    public static void checkPermission(Context context, String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
}