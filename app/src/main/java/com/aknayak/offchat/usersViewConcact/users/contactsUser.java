package com.aknayak.offchat.usersViewConcact.users;

import java.io.Serializable;

public class contactsUser implements Serializable {
    private String userName;
    private String phoneNumber;
    private String dp_photoAddress;

    public contactsUser(String userName, String phoneNumber, String dp_photoAddress) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.dp_photoAddress = dp_photoAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDp_photoAddress() {
        return dp_photoAddress;
    }

    public void setDp_photoAddress(String dp_photoAddress) {
        this.dp_photoAddress = dp_photoAddress;
    }
}
