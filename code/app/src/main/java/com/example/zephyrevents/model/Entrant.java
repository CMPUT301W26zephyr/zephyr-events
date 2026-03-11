package com.example.zephyrevents.model;

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