package com.aknayak.offchat.users;

import androidx.annotation.Keep;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

@Keep
public class connDetail{
    private Boolean cnDetails;

    public connDetail(Boolean connected) {
        cnDetails = connected;
    }

    public connDetail() {

    }

    public Boolean getCnDetails() {
        return cnDetails;
    }

    public void setCnDetails(Boolean cnDetails) {
        this.cnDetails = cnDetails;
    }
}
