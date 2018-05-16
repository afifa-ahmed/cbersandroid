package com.example.cbers.common;

public enum StatusListItem {

    TEMPERATURE("Temparature"), HEART_RATE("Heart Rate"), BP("Blood Pressure"), SUGAR("Sugar");

    String item;

    StatusListItem(String item) {
        this.item = item;

    }

    @Override
    public String toString() {
        return item;
    }
}
