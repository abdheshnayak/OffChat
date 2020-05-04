package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.aknayak.offchat.globaldata.respData;
public class notificationDialog extends AppCompatActivity {


    TextView userName;
    TextView userMessage;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein,R.anim.fadeout);
        setContentView(R.layout.activity_notification_dialog);
        this.setFinishOnTouchOutside(true);
        getWindow().getAttributes().gravity = Gravity.TOP;


        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 5000);

        respData.mNotificationApp = this;

        userName = findViewById(R.id.notification_name);
        userMessage = findViewById(R.id.notification_message);

        userName.setText(getIntent().getStringExtra("userName"));
        userMessage.setText(getIntent().getStringExtra("userMessage"));

        findViewById(R.id.notifyScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
    }

    public void refreshData(String title,String msg){
        userName.setText(title);
        userMessage.setText(msg);
    }
    @Override
    protected void onResume() {
        super.onResume();
        respData.mNotificationApp = this;
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
            respData.mNotificationApp = null;
    }

}
