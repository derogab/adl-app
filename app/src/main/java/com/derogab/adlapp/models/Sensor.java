package com.derogab.adlapp.models;

public class Sensor {

    private String sensor;
    private boolean enabled;

    public Sensor(String sensor, boolean enabled) {
        this.sensor = sensor;
        this.enabled = enabled;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "sensor='" + sensor + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
