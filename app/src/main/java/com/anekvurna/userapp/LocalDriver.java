package com.anekvurna.userapp;

/**
 * Created by Admin on 1/11/2018.
 */

public class LocalDriver {
    private String name, mobile, userId, elementId;
    private boolean isCurrent;


    public LocalDriver(String name, String mobile, String userId, String elementId) {
        this.name = name;
        this.mobile = mobile;
        this.userId = userId;
        this.elementId = elementId;
        isCurrent = false;
    }

    public LocalDriver()
    {
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUserId() {
        return userId;
    }

    public String getElementId() {
        return elementId;
    }

}
