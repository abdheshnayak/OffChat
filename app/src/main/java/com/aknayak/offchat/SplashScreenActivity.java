package com.aknayak.offchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/30/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No need to check for live connection here.

        // Send user to appropriate screen:
        // 1. If we have an account and no credential validation is needed, send to MainActivity.
        // 2. If we don't have an account or credential validation is required send to LoginActivity.
        Intent launch = new Intent(this, FirebaseAuth.getInstance().getCurrentUser()!=null? MainActivity.class:phone_verification.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getWindow().setWindowAnimations(R.style.DialogAnimation);
        startActivity(launch);
        finish();
    }
}
