package com.aknayak.offchat.users;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String userName;
    private Date lastMessageSentTime = Calendar.getInstance().getTime();
    private String lastMessage;
    private String dp_photoAddress;
    private int sentStatus;

    public User() {

    }


    public User(String userName, Date lastMessageSentTime, String lastMessage, String dp_photoAddress, int sentStatus) {
        this.userName = userName;
        this.lastMessageSentTime = lastMessageSentTime;
        this.lastMessage = lastMessage;
        this.dp_photoAddress = dp_photoAddress;
        this.sentStatus = sentStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getLastMessageSentTime() {
        return lastMessageSentTime;
    }

    public void setLastMessageSentTime(Date lastMessageSentTime) {
        this.lastMessageSentTime = lastMessageSentTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getDp_photoAddress() {
        return dp_photoAddress;
    }

    public void setDp_photoAddress(String dp_photoAddress) {
        this.dp_photoAddress = dp_photoAddress;
    }

    public int getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(int sentStatus) {
        this.sentStatus = sentStatus;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("dp_photoAddress", dp_photoAddress);
        userUpdates.put("lastMessage", lastMessage);
        userUpdates.put("lastMessageSentTime", lastMessageSentTime);
        userUpdates.put("sentStatus", sentStatus);
        userUpdates.put("userName", userName);
        return userUpdates;
    }
}
