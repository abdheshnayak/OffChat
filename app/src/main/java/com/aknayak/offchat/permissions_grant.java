package com.aknayak.offchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class permissions_grant extends AppCompatActivity implements View.OnClickListener {

    Button permissions, startapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_grant);
        permissions = findViewById(R.id.givePermissions);
        startapp = findViewById(R.id.startApp);
        permissions.setOnClickListener(this);
        startapp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.givePermissions:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
            case R.id.startApp:
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
        }
    }
}
