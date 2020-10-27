package com.example.frontend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;

public class Event {

    private Date date;
    private String events;

    public Event(Date date, String events) {
        this.date = date;
        this.events = events;

    }

    public Date getDate() {
        return date;
    }

    public String getEvents() {
        return events;
    }
}