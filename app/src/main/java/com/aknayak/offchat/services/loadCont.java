package com.aknayak.offchat.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import static com.aknayak.offchat.Constants.ROOT_CHILD;
import static com.aknayak.offchat.globaldata.respData.getAllContacts;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/29/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class loadCont {




    public static void loadCont(Context context){
        final ArrayList<contactsUser> mobileArray = new ArrayList<>();

        DatabaseReference dbfr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status");

        mobileArray.addAll(getAllContacts(context.getContentResolver()));
        final DBHelper mydb = new DBHelper(context.getApplicationContext());

//        mydb.deleteAllContact();
        try {
            for (int i = 0; i < mobileArray.size(); i++) {
                final String name = mobileArray.get(i).getUserName();
                final String phone = mobileArray.get(i).getPhoneNumber();
                dbfr.child(phone).child("online_status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(Date.class) != null) {
                            Log.d("abdhesh", "true " + phone);
                            mydb.insertContact(name, phone, true);
                        } else {
                            Log.d("abdhesh", "false" + phone);
                            mydb.insertContact(name, phone, false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            Log.d("contLoad", e.getMessage());
        }
    }

}
