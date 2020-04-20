package com.aknayak.offchat.users;

public class connDetail {
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
