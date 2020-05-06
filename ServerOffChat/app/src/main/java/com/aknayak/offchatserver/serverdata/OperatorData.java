package com.aknayak.offchatserver.serverdata;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OffChat
 * Created by Abdhesh Nayak on 5/2/20
 * abdheshnayak@gmail.com
 **/
@Keep
public class OperatorData {
    private ArrayList<String> opCode;

    public OperatorData(ArrayList<String> operatorCode) {
        opCode = operatorCode;
    }

    public ArrayList<String> getOpCode() {
        return opCode;
    }

    public void setOpCode(ArrayList<String> opCode) {
        this.opCode = opCode;
    }

    public OperatorData(){

    }
    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("opCode", opCode);
        return userUpdates;
    }

}
