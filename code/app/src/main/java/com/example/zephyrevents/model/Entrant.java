package com.example.zephyrevents.model;

/**
 * Data holder with public fields used to represent Entrants in Views/EntrantAdapter
 */
public class Entrant {
    public String name;
    public String detail;
    public boolean showCancel;

    public Entrant(String name, String detail, boolean showCancel) {
        this.name = name;
        this.detail = detail;
        this.showCancel = showCancel;
    }
}