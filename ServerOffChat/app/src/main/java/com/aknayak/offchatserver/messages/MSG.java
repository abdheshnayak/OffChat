package com.aknayak.offchatserver.messages;

/**
 * OffChat
 * Created by Abdhesh Nayak on 5/5/20
 * abdheshnayak@gmail.com
 **/
public class MSG {
    String message;
    String senderPhone;
    String receiverPhone;
    int timestamp;
    int status;

    public MSG(String message, String senderPhone, String receiverPhone, int timestamp, int status) {
        this.message = message;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
