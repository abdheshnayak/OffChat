package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class splashScreen extends AppCompatActivity {

    TextView version;
    TextView copyRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        version = findViewById(R.id.version);
        copyRight = findViewById(R.id.copyRight);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY");
        copyRight.setText("COPYRIGHT "+simpleDateFormat.format(Calendar.getInstance().getTime())+" OffChat Inc.");
        version.setText("Version "+MainActivity.forceUpdateVersion+"."+MainActivity.normalupdateVersion);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }
}
