package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.aknayak.offchat.globaldata.Constants.forceUpdateVersion;
import static com.aknayak.offchat.globaldata.Constants.normalupdateVersion;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class aboutPage extends AppCompatActivity {

    TextView version;
    TextView copyRight;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        version = findViewById(R.id.version);
        copyRight =findViewById(R.id.licence);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY");
        copyRight.setText("COPYRIGHT "+simpleDateFormat.format(Calendar.getInstance(Locale.ENGLISH).getTime())+" OffChat Inc.");
        version.setText("Version "+forceUpdateVersion+"."+normalupdateVersion);
    }
}
