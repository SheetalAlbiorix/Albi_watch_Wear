package com.wearos.model;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wearos.Env;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthRepository {

    private static final String TAG = "GoogleFitDemo";

    private static FirebaseAuth auth;

    FitnessOptions fitnessOptions;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
    }

    FirebaseAuth getAuth() {
        return auth;
    }

    void signOut() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
    }

    public void signIn(Activity context, int requestCode) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Env.googleSignServerClientId)
                .requestEmail()
                .build();
        GoogleSignInClient signInIntent = GoogleSignIn.getClient(context, gso);

        context.startActivityForResult(signInIntent.getSignInIntent(), requestCode);
    }

    public void onActivityResult(Activity activity, int requestCode, int userRequestCode, int resultCode, Intent data) {
        // When request code is equal to 100 initialize task
        try {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = signInAccountTask.getResult();
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                        activity,
                        235, // Define this as a constant, e.g., 1001
                        account,
                        fitnessOptions
                );
            } else {
                // Permissions already granted
                if (requestCode == userRequestCode) {
                    // check condition
                    if (signInAccountTask.isSuccessful()) {
                        // Check condition
                        if (account != null) {
                            // When sign in account is not equal to null initialize auth credential
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccountTask.getResult().getIdToken(), null);
                            accessGoogleFitApi(account, activity);
                            // Check credential
                            auth.signInWithCredential(authCredential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // Check condition
                                    if (task.isSuccessful()) {
                                        // When task is successful redirect to profile activity display Toast
                                        Log.e(TAG, "Firebase authentication successful");
                                    } else {
                                        // When task is unsuccessful display Toast
                                        Log.e(TAG, "Authentication Failed IN :" + task.getException().getMessage());
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Authentication failed" + Objects.requireNonNull(signInAccountTask.getException()).getLocalizedMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Authentication failed : " + e.getLocalizedMessage());
        }
    }

    private void accessGoogleFitApi(GoogleSignInAccount account, Activity activity) {
        // Use the account to access the Google Fit API
        // For example, you can use the HistoryClient to retrieve step county
        GoogleSignIn.getAccountForExtension(activity, fitnessOptions);
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7), System.currentTimeMillis(), TimeUnit.SECONDS)
                .build();
        Fitness.getHistoryClient(activity, account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        if (!dataReadResponse.getBuckets().isEmpty()) {
                            // Get step count from buckets
                            for (Bucket bucket : dataReadResponse.getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                        int stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                        // Update UI with step count from Galaxy Watch 4
                                        Log.d(TAG, "Step count from bucket: " + stepCount);
                                    }
                                }
                            }
                        } else if (!dataReadResponse.getDataSets().isEmpty()) {
                            // Get step count from data sets
                            for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                    int stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                    // Update UI with step count from Galaxy Watch 4
                                    Log.d(TAG, "Step count from data set: " + stepCount);
                                }
                            }
                        } else {
                            Log.d(TAG, "Step count from data set ELSE: " + dataReadResponse);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to read step count", e);
                    }
                });
    }
}
