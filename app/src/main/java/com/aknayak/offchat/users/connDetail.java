package com.aknayak.offchat.users;

import java.io.Serializable;

public class connDetail implements Serializable {
    private Boolean Connected;

    public connDetail(Boolean connected) {
        Connected = connected;
    }

    public connDetail(){

    }
    public Boolean getConnected() {
        return Connected;
    }

    public void setConnected(Boolean connected) {
        Connected = connected;
    }
}
