package com.aknayak.offchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.aknayak.offchat.datas.DBHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchat.globaldata.Constants.ROOT_CHILD;
import static com.aknayak.offchat.globaldata.respData.receiverUsername;
import static com.aknayak.offchat.globaldata.respData.showAds;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class profileCard extends AppCompatActivity {

    TextView mNumber;
    TextView mOnline;
    TextView user_Name;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_card);
        mNumber = findViewById(R.id.number);
        mOnline = findViewById(R.id.lastSeen);
        user_Name = findViewById(R.id.name);
        mNumber.setText(getIntent().getStringExtra("phone"));
        DBHelper mydb = new DBHelper(this);

        adView = findViewById(R.id.adView);

        if (showAds) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setVisibility(View.VISIBLE);
            adView.loadAd(adRequest);
        }


        final DatabaseReference userNameRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(receiverUsername).child("username");
        DatabaseReference o_status = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(receiverUsername).child("online_status");

        ValueEventListener v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String UserName = dataSnapshot.getValue(String.class);
                if (UserName != null && !UserName.equals("")) {
                    user_Name.setText(UserName);
                } else {
                    user_Name.setText("No available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ValueEventListener v2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Date date = dataSnapshot.getValue(Date.class);
                if (date != null) {
                    Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                    long diff = 0;

                    diff = currentTime.getTime() - date.getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;
                    mOnline.setVisibility(View.VISIBLE);
                    if (diffMinutes < 1) {
                        mOnline.setText("online");
                    } else {
                        long dayCheck = date.getDay() - currentTime.getDay();
                        String str;
                        if (dayCheck == 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa");
                            str = sf.format(date);
                            str = "today " + str;
                        } else if (dayCheck == 1) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa");
                            str = sf.format(date);
                            str = "yesterday " + str;
                        } else {
                            SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd  hh:mm aa");
                            str = sf.format(date);
                        }
                        String str2 = str.replace("AM", "am").replace("PM", "pm");
                        str2 = "last seen " + str2;
                        mOnline.setText(str2);
                    }
                } else {
                    mOnline.setVisibility(View.VISIBLE);
                    mOnline.setText("Not on OffChat");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        if (mNumber.getText().equals(mydb.getUserName(getIntent().getStringExtra("phone")))) {
            user_Name.setText("No available");
            userNameRef.addListenerForSingleValueEvent(v1);
        } else {
            user_Name.setText(mydb.getUserName(getIntent().getStringExtra("phone")));
        }

        o_status.addValueEventListener(v2);
    }
}
