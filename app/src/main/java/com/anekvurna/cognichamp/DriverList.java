package com.anekvurna.cognichamp;

/**
 * Created by Admin on 1/11/2018.
 */

public class DriverList {
    private String username, mobile, elementId, userId;

    public DriverList(String username, String mobile, String elementId, String userId) {
        this.username = username;
        this.mobile = mobile;
        this.elementId = elementId;
        this.userId = userId;
    }

    public DriverList()
    {
    }

    public String getUsername() {
        return username;
    }

    public String getMobile() {
        return mobile;
    }

    public String getElementId() {
        return elementId;
    }

    public String getUserId() {
        return userId;
    }

}
