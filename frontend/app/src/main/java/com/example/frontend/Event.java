package com.example.frontend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.threeten.bp.LocalDate;
import java.util.Locale;

import android.graphics.Bitmap;

public class Event {

    private LocalDate date;
    private String events;

    public Event(LocalDate date, String events) {
        this.date = date;
        this.events = events;

    }

    public LocalDate getDate() {
        return date;
    }

    public String getEvents() {
        return events;
    }
}