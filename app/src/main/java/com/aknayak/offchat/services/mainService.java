package com.aknayak.offchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aknayak.offchat.MainActivity;
import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.globaldata.AESHelper.decrypt;
import static com.aknayak.offchat.globaldata.respData.MAINVIEW_CHILD;
import static com.aknayak.offchat.globaldata.respData.MESSAGES_CHILD;
import static com.aknayak.offchat.globaldata.respData.getRoot;
import static com.aknayak.offchat.globaldata.respData.mUsername;
import static com.aknayak.offchat.globaldata.respData.notifyIt;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/23/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class mainService extends Service {

    String st;
    ValueEventListener v1;
    ValueEventListener v2;
    DBHelper mydb = new DBHelper(this);
    DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD);
    DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD);

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(getApplicationContext(),"runnign",Toast.LENGTH_SHORT).show();


        v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        if (!mydb.getHist().equals(dataSnapshot.getChildrenCount())) {
                            ArrayList<String> arList = new ArrayList<>();
                            for (Message tmpmsg : mydb.getHist()) {
                                arList.add(tmpmsg.getMessageSource().equals(mUsername) ? tmpmsg.getMessageFor() : tmpmsg.getMessageSource());
                            }
                            st = snapshot.getKey();
                            if (!arList.contains(snapshot.getKey())) {
                                fdbr.child(getRoot(st, mUsername)).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Message message = snapshot.getValue(Message.class);
                                            if (message.getMessageStatus() == 1 && !message.getMessageSource().equals(mUsername)) {
                                                message.setMessageStatus(2);
                                                fdbr.child(getRoot(message.getMessageSource(),message.getMessageFor())).child(snapshot.getKey()).child("messageStatus").setValue(2);

                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);

                                                if (MainActivity.temp == null || (MainActivity.temp != null && !MainActivity.temp.equals(message.getMessageSource()))) {
//                                                    Toast.makeText(getApplicationContext(),MainActivity.temp+message.getMessageSource(),Toast.LENGTH_SHORT).show();
                                                    notifyIt("" + mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()), getApplicationContext(), Double.valueOf(message.getMessageSource()).intValue() + Double.valueOf(simpleDateFormat.format(message.getMessageSentTime())).intValue());
                                                }
//                                                showNotification(mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()),getApplicationContext());
                                                Log.d("LLLL", "" + Double.valueOf(message.getMessageSource()).intValue() + message.getMessage());
                                            }
                                            if (message != null && (message.getMessageSource().equals(mUsername) || message.getMessageStatus() != 1)) {
                                                mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor(),message.getReplyId());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        v2 = new  ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getMessageStatus() == 1 && !message.getMessageSource().equals(mUsername)) {
                        message.setMessageStatus(2);
                        fdbr.child(getRoot(message.getMessageSource(),message.getMessageFor())).child(snapshot.getKey()).child("messageStatus").setValue(2);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);

                        if (MainActivity.temp == null || (MainActivity.temp != null && !MainActivity.temp.equals(message.getMessageSource()))) {
//                                    Toast.makeText(getApplicationContext(),MainActivity.temp+message.getMessageSource(),Toast.LENGTH_SHORT).show();
                            notifyIt("" + mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()), getApplicationContext(), Double.valueOf(message.getMessageSource()).intValue() + Double.valueOf(simpleDateFormat.format(message.getMessageSentTime())).intValue());
                        }
//                              showNotification(mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()),getApplicationContext());
                        Log.d("LLLL", "" + Double.valueOf(message.getMessageSource()).intValue() + message.getMessage());
                    }
                    if (message != null && (!message.getMessageSource().equals(mUsername) || message.getMessageStatus() != 1)) {
                        mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor(), message.getReplyId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mUsername = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        mUsername = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        try {
            for (Message msg : mydb.getHist()) {

                final String msgSource = msg.getMessageSource();
                fdbr.child(getRoot((msg.getMessageSource().equals(mUsername) ? msg.getMessageFor() : msg.getMessageSource()), mUsername)).addValueEventListener(v2);

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        historyRef.child(mUsername).addValueEventListener(v1);


    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Log.d("service","destroyed");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Toast.makeText(getApplicationContext(),"Bind",Toast.LENGTH_SHORT).show();
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
