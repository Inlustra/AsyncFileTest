package com.thenairn.asynctest.entity;

import java.util.Date;

/**
 * Simple POJO containing the date at which the string was added to the object.
 */
public class LogMessage {

    private Date date;
    private String message;

    public LogMessage(String message) {
        this.date = new Date();
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }
}
