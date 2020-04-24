package com.aknayak.offchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.globaldata.AESHelper.decrypt;
import static com.aknayak.offchat.globaldata.respData.MAINVIEW_CHILD;
import static com.aknayak.offchat.globaldata.respData.MESSAGES_CHILD;
import static com.aknayak.offchat.globaldata.respData.mUsername;
import static com.aknayak.offchat.globaldata.respData.notifyIt;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/23/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class mainService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("servicea","start");
        final DBHelper mydb = new DBHelper(getApplicationContext());

//        FirebaseApp.initializeApp(this);
        mUsername = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        try {
            for (Message msg : mydb.getHist()) {

                final String msgSource = msg.getMessageSource();

                final DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot((msg.getMessageSource().equals(mUsername) ? msg.getMessageFor() : msg.getMessageSource()), mUsername));
                fdbr.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message.getMessageStatus() == 1 && !message.getMessageSource().equals(mUsername)) {
                                message.setMessageStatus(2);
                                fdbr.child(snapshot.getKey()).child("messageStatus").setValue(2);

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);

                                if( MainActivity.temp == null || (MainActivity.temp != null && !MainActivity.temp.equals(message.getMessageSource())) )
                                {
                                notifyIt("" + mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()), getApplicationContext(), Double.valueOf(message.getMessageSource()).intValue() + Double.valueOf(simpleDateFormat.format(message.getMessageSentTime())).intValue());
                                }
//                              showNotification(mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()),getApplicationContext());
                                Log.d("LLLL", "" + Double.valueOf(message.getMessageSource()).intValue() + message.getMessage());
                            }
                            if (message != null && (!message.getMessageSource().equals(mUsername) || message.getMessageStatus() != 1)) {
                                mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor(),2);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        ValueEventListener v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        if (!mydb.getHist().equals(dataSnapshot.getChildrenCount())){
                            ArrayList<String> arList = new ArrayList<>();
                            for (Message tmpmsg : mydb.getHist()){
                                arList.add(tmpmsg.getMessageSource().equals(mUsername) ? tmpmsg.getMessageFor() : tmpmsg.getMessageSource());
                            }
                            String st = snapshot.getKey();
                            if (!arList.contains(snapshot.getKey())){
                                final DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot( st , mUsername));
                                fdbr.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Message message = snapshot.getValue(Message.class);
                                            if (message.getMessageStatus() == 1 && !message.getMessageSource().equals(mUsername)) {
                                                message.setMessageStatus(2);
                                                fdbr.child(snapshot.getKey()).child("messageStatus").setValue(2);

                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);

                                                if( MainActivity.temp == null || (MainActivity.temp != null && !MainActivity.temp.equals(message.getMessageSource())) )
                                                {
                                                    notifyIt("" + mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()), getApplicationContext(), Double.valueOf(message.getMessageSource()).intValue() + Double.valueOf(simpleDateFormat.format(message.getMessageSentTime())).intValue());
                                                }
//                                                showNotification(mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()),getApplicationContext());
                                                Log.d("LLLL", "" + Double.valueOf(message.getMessageSource()).intValue() + message.getMessage());
                                            }
                                            if (message != null && (message.getMessageSource().equals(mUsername) || message.getMessageStatus() != 1)) {
                                                mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor(),3);
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


        mUsername = FirebaseAuth.getInstance().

                getCurrentUser().

                getPhoneNumber();

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(respData.mUsername);
        historyRef.addValueEventListener(v1);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(getApplicationContext(),"Destroy",Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Toast.makeText(getApplicationContext(),"Bind",Toast.LENGTH_SHORT).show();
        return null;
    }
}
