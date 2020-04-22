package com.aknayak.offchat.users;

import java.io.Serializable;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class connDetail implements Serializable {
    private Boolean Connected;

    public connDetail(Boolean connected) {
        Connected = connected;
    }

    public connDetail() {

    }

    public Boolean getConnected() {
        return Connected;
    }

    public void setConnected(Boolean connected) {
        Connected = connected;
    }
}
