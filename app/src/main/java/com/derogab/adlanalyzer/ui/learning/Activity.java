package com.derogab.adlanalyzer.ui.learning;

public class Activity {

    public static final String SENSOR_GYROSCOPE = "gyroscope";
    public static final String SENSOR_ACCELEROMETER = "accelerometer";

    private String name;
    private int seconds;
    private double frequency;
    private String[] sensors;

    public Activity(String name) {
        this.name = name;
    }

    public Activity(String name, int seconds) {
        this.name = name;
        this.seconds = seconds;
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

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public String[] getSensors() {
        return sensors;
    }

    public void setSensors(String[] sensors) {
        this.sensors = sensors;
    }

    @Override
    public String toString() {
        return name;
    }
}
