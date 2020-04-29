package com.aknayak.offchat.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.internal.GmsClientEventManager;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/25/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("abdhesh", "hello");

//        String status = NetworkUtil.getConnectivityStatusString(context);
        Intent i = new Intent(com.aknayak.offchat.services.mainService.class.getName());
        i.setPackage(context.getPackageName());
        context.startService(i);
//        Toast.makeText(context,""+status, Toast.LENGTH_LONG).show();
    }
}