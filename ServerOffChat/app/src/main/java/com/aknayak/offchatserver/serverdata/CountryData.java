package com.aknayak.offchatserver.serverdata;

import androidx.annotation.Keep;

import java.util.HashMap;
import java.util.Map;

/**
 * OffChat
 * Created by Abdhesh Nayak on 5/2/20
 * abdheshnayak@gmail.com
 **/
@Keep
public class CountryData {
    private String cntCode;
    private Map<String,OperatorData> operaters;

    public CountryData(String code, Map<String, OperatorData> operaters) {
        cntCode = code;
        this.operaters = operaters;
    }

    public String getCntCode() {
        return cntCode;
    }

    public void setCntCode(String cntCode) {
        this.cntCode = cntCode;
    }

    public Map<String, OperatorData> getOperaters() {
        return operaters;
    }

    public void setOperaters(Map<String, OperatorData> operaters) {
        this.operaters = operaters;
    }

    public CountryData(){

    }
    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("operators",operaters);
        userUpdates.put("cntCode", cntCode);
        return userUpdates;
    }
}
