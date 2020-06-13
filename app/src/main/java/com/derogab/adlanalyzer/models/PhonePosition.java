package com.derogab.adlanalyzer.models;

public class PhonePosition {

    public static final String IN_LEFT_HAND = "left_hand";
    public static final String IN_RIGHT_HAND = "right_hand";
    public static final String IN_THE_FRONT_LEFT_POCKET = "front_left_pocket";
    public static final String IN_THE_FRONT_RIGHT_POCKET = "back_left_pocket";
    public static final String IN_THE_BACK_LEFT_POCKET = "front_right_pocket";
    public static final String IN_THE_BACK_RIGHT_POCKET = "back_right_pocket";

    private long id;
    private String position;
    private String description;

    public PhonePosition(long id, String position, String description) {
        this.id = id;
        this.position = position;
        this.description = description;
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
