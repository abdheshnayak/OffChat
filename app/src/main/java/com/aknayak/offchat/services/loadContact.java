package com.aknayak.offchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.ArrayList;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.receiverUsername;
import static com.aknayak.offchat.globaldata.respData.getAllContacts;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/26/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class loadContact extends Service {
    ArrayList<contactsUser> mobileArray = new ArrayList<>();
    DBHelper mydb = new DBHelper(this);

    DatabaseReference dbfr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status");

    @Override
    public void onCreate() {
        super.onCreate();
//        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mobileArray.addAll(getAllContacts(getContentResolver()));

        try {
            for (int i = 0; i < mobileArray.size(); i++) {
                final String name = mobileArray.get(i).getUserName();
                final String phone = mobileArray.get(i).getPhoneNumber();
                dbfr.child(phone).child("online_status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(Date.class) != null){
                            Log.d("abdhesh","true "+phone );
                            mydb.insertContact(name, phone,true);
                        }else {
                            Log.d("abdhesh","false"+ phone);
                            mydb.insertContact(name, phone,false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }catch (Exception e){
            Log.d("contLoad",e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
