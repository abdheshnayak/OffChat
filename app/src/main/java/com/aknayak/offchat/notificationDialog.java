package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionValues;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Delayed;

public class notificationDialog extends AppCompatActivity {

    TextView userName;
    TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        setContentView(R.layout.activity_notification_dialog);
        this.setFinishOnTouchOutside(true);
        getWindow().getAttributes().gravity = Gravity.TOP;

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 5000);

        userName = findViewById(R.id.notification_name);
        message = findViewById(R.id.notification_message);

        userName.setText(getIntent().getStringExtra("userName"));
        message.setText(getIntent().getStringExtra("userMessage"));

        findViewById(R.id.notifyScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userName.setText(getIntent().getStringExtra("userName"));
        message.setText(getIntent().getStringExtra("userMessage"));
    }
}
