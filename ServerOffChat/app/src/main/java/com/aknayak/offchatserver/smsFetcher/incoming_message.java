package com.aknayak.offchatserver.smsFetcher;

/**
 * OffChat
 * Created by Abdhesh Nayak on 5/5/20
 * abdheshnayak@gmail.com
 **/
public class incoming_message {
    String number;
    String message;
    String msgId;

    public incoming_message(String number, String message, String msgId) {
        this.number = number;
        this.message = message;
        this.msgId = msgId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
