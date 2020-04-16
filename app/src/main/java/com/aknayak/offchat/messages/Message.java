package com.aknayak.offchat.messages;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.aknayak.offchat.MainActivity.receiverUsername;

public class Message implements Serializable {
    private String Message;
    private String messageSource;
    private Date messageSentTime;
    private int messageStatus;
    private String messageID;

    public Message(String message, String messageSource, int messageStatus,String messageID) {
        this.messageSentTime= Calendar.getInstance().getTime();
        this.Message = message;
        this.messageSource = messageSource;
        this.messageStatus= messageStatus;
        this.messageID=messageID;
    }

    public Message(String message, String messageSource,Date messageSentTime, int messageStatus,String messageID) {
        this(message,messageSource,messageStatus,messageID);
        this.messageSentTime= messageSentTime;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(String messageSource) {
        this.messageSource = messageSource;
    }

    public Message(){

    }

    public Date getMessageSentTime() {
        return messageSentTime;
    }

    public void setMessageSentTime(Date messageSentTime) {
        this.messageSentTime = messageSentTime;
    }

    public Map<String, Object> toMap(){
        Map<String,Object> userUpdates = new HashMap<>();
        userUpdates.put("message",Message);
        userUpdates.put("messageSentTime",messageSentTime);
        userUpdates.put("messageSource",messageSource);
        userUpdates.put("messageStatus",messageStatus);
        userUpdates.put("messageId",messageID);
        return userUpdates;
    }
}
