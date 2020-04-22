package com.aknayak.offchat.versionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * OffChat
 * Created by Abdhesh Nayak on 4/23/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/
public class appVersion {
    private String normalUpdateVersion;
    private String forceupdateVersion;

    public appVersion() {

    }

    public appVersion(String normalUpdateVersion, String forceupdateVersion) {
        this.normalUpdateVersion = normalUpdateVersion;
        this.forceupdateVersion = forceupdateVersion;
    }

    public String getNormalUpdateVersion() {
        return normalUpdateVersion;
    }

    public void setNormalUpdateVersion(String normalUpdateVersion) {
        this.normalUpdateVersion = normalUpdateVersion;
    }

    public String getForceupdateVersion() {
        return forceupdateVersion;
    }

    public void setForceupdateVersion(String forceupdateVersion) {
        this.forceupdateVersion = forceupdateVersion;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("normalUpdateVersion", normalUpdateVersion);
        userUpdates.put("forceupdateVersion", forceupdateVersion);
        return userUpdates;
    }
}
