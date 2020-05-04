package com.aknayak.offchat.users;

import androidx.annotation.Keep;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/23/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
@Keep
public class typingDetails {
    public boolean typing=false;
    public Date time;


    public typingDetails(){

    }

    public typingDetails(boolean typing, Date time) {
        this.typing = typing;
        this.time = time;
    }

    public typingDetails(boolean typing) {
        this.typing = typing;
        this.time = Calendar.getInstance(Locale.ENGLISH).getTime();
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("typing", typing);
        userUpdates.put("time", time);
        return userUpdates;
    }
}
