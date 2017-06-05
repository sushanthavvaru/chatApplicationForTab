package com.example.sushanth.identifyuser;

import java.util.Date;

/**
 * Created by sushanth on 5/1/2017.
 */

//reference https://www.youtube.com/watch?v=w-zXAcu5OsA

public class Message {

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    private String text;
    private String sender;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private Date date;

}
