package com.aknayak.offchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;

import java.util.ArrayList;

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

    @Override
    public void onCreate() {
        super.onCreate();

//        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mobileArray.addAll(getAllContacts(getContentResolver()));

        for (int i = 0; i < mobileArray.size(); i++) {
            mydb.insertContact(mobileArray.get(i).getUserName(), mobileArray.get(i).getPhoneNumber());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
