package com.example.frontend;

import java.util.ArrayList;

public class Clothes {
    private String id;
    private String category;
    private String color;
    private String name;
    private String updated;
    private String user;
    private ArrayList<String> seasons;
    private ArrayList<String> occasions;

    public Clothes(String id, String category, String color, String name, String updated,
                   String user, ArrayList<String> seasons, ArrayList<String> occasions) {
        this.id = id;
        this.category = category;
        this.color = color;
        this.name = name;
        this.updated = updated;
        this.user = user;
        this.seasons = seasons;
        this.occasions = occasions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ArrayList<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(ArrayList<String> seasons) {
        this.seasons = seasons;
    }

    public ArrayList<String> getOccasions() {
        return occasions;
    }

    public void setOccasions(ArrayList<String> occasions) {
        this.occasions = occasions;
    }
}
