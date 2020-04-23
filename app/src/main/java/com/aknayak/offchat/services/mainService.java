package com.aknayak.offchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.users.connDetail;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.globaldata.AESHelper.decrypt;
import static com.aknayak.offchat.globaldata.respData.mUsername;
import static com.aknayak.offchat.globaldata.respData.notifyIt;
import static com.aknayak.offchat.messageViewActivity.MAINVIEW_CHILD;
import static com.aknayak.offchat.messageViewActivity.MESSAGES_CHILD;

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

        final DBHelper mydb = new DBHelper(getApplicationContext());

        ValueEventListener v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    connDetail check = snapshot.getValue(connDetail.class);
                    if (check != null && check.getConnected()) {
                        String user = snapshot.getKey();
                        if (user != null) {
                            final DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(user, mUsername));
                            fdbr.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Message message = snapshot.getValue(Message.class);
                                        if (message.getMessageStatus() == 1 && message.getMessageSource().equals(message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource())) {
                                            message.setMessageStatus(2);
                                            fdbr.child(snapshot.getKey()).child("messageStatus").setValue(2);

                                            //                Updating History Of Sender
//                        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(message.getMessageSource().equals(senderUserName)?message.getMessageFor():message.getMessageSource()).child(senderUserName).child("sentStatus");
//                        mFirebaseDatabaseReference.setValue(2);
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);

                                            notifyIt(R.drawable.ic_launcher_empty, "" + mydb.getUserName(message.getMessageSource()), decrypt(message.getMessage()), getApplicationContext(), Double.valueOf(message.getMessageSource()).intValue() + Double.valueOf(simpleDateFormat.format(message.getMessageSentTime())).intValue());
                                            Log.d("LLLL", "" + Double.valueOf(message.getMessageSource()).intValue() + message.getMessage());
                                        }
                                        if (message != null && (message.getMessageSource().equals(message.getMessageFor()) || message.getMessageStatus() != 1)) {
                                            mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        mUsername= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(respData.mUsername);
        historyRef.addValueEventListener(v1);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
