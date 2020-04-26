package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class aboutPage extends AppCompatActivity {

    TextView version;
    TextView copyRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        version = findViewById(R.id.version);
        copyRight =findViewById(R.id.copyRight);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY");
        copyRight.setText("COPYRIGHT "+simpleDateFormat.format(Calendar.getInstance().getTime())+" OffChat Inc.");
        version.setText("Version "+MainActivity.forceUpdateVersion+"."+MainActivity.normalupdateVersion);
    }
}
