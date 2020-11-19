package com.example.frontend;


import org.threeten.bp.LocalDate;

public class Event {

    private LocalDate date;
    private String summary;

    public Event(LocalDate date, String summary) {
        this.date = date;
        this.summary = summary;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSummary() {
        return summary;
    }
}