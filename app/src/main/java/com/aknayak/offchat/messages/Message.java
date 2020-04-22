package com.aknayak.offchat.messages;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.aknayak.offchat.MainActivity.receiverUsername;

public class Message implements Serializable {
    private String Message;
    private String messageSource;
    private Date messageSentTime;
    private int messageStatus;
    private String messageID;
    private String messageFor;


    public String getMessageFor() {
        return messageFor;
    }

    public void setMessageFor(String messageFor) {
        this.messageFor = messageFor;
    }

    public Message(String message, String messageSource, Date messageSentTime, int messageStatus, String messageID, String messageFor) {
        this.messageSentTime = messageSentTime;
        this.Message = message;
        this.messageSource = messageSource;
        this.messageStatus = messageStatus;
        this.messageID = messageID;
        this.messageFor = messageFor;
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

    public Message() {

    }

    public Date getMessageSentTime() {
        return messageSentTime;
    }

    public void setMessageSentTime(Date messageSentTime) {
        this.messageSentTime = messageSentTime;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("message", Message);
        userUpdates.put("messageSentTime", messageSentTime);
        userUpdates.put("messageSource", messageSource);
        userUpdates.put("messageStatus", messageStatus);
        userUpdates.put("messageId", messageID);
        userUpdates.put("messageFor", messageFor);
        return userUpdates;
    }
}
