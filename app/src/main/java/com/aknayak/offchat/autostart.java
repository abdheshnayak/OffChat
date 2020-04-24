package com.aknayak.offchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.aknayak.offchat.services.mainService;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/24/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class autostart extends BroadcastReceiver {
    public void onReceive(Context context, Intent arg1)
    {
        Log.d("Autostart", "started");
        Intent intent = new Intent(context, mainService.class);
        context.startService(intent);
    }
}
