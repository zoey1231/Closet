package com.example.frontend;


import org.threeten.bp.LocalDate;

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