package com.derogab.adlanalyzer.models;

import java.util.List;

public class Activity {

    private String activity;
    private int time;
    private List<Sensor> sensors;

    public Activity(String activity, int time, List<Sensor> sensors) {
        this.activity = activity;
        this.time = time;
        this.sensors = sensors;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public boolean isSensorActive(String sensor) {

        for (int i = 0; i < sensors.size(); i++)
            if (sensors.get(i).getSensor().equals(sensor))
                return sensors.get(i).isEnabled();

        return false;

    }

    @Override
    public String toString() {
        return getActivity();
    }

}
