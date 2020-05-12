package com.derogab.adlanalyzer.utils;

public class Activity {

    public static final String SENSOR_GYROSCOPE = "gyroscope";
    public static final String SENSOR_ACCELEROMETER = "accelerometer";

    // Activity Info
    private String name;
    private int seconds;
    // Usable sensors
    private boolean accelerometer;
    private boolean gyroscope;

    public Activity(String name) {
        this.name = name;
    }

    public Activity(String name, int seconds) {
        this.name = name;
        this.seconds = seconds;
        accelerometer = true; // true by default
        gyroscope = true;  // true by default
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public boolean canUse(String sensor) {

        switch (sensor) {
            case SENSOR_ACCELEROMETER: return accelerometer;
            case SENSOR_GYROSCOPE: return gyroscope;
            default: return false;
        }

    }

    public void canUse(String sensor, boolean use) {

        switch (sensor) {
            case SENSOR_ACCELEROMETER: accelerometer = use; break;
            case SENSOR_GYROSCOPE: gyroscope = use; break;
        }

    }

    @Override
    public String toString() {
        return name;
    }
}
