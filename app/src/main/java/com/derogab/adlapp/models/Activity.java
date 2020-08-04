package com.derogab.adlapp.models;

import com.derogab.adlapp.utils.CurrentLang;

import java.util.List;

public class Activity {

    private long id;
    private String activity;
    private Translation translations;
    private int time;
    private List<Sensor> sensors;

    public Activity(long id, String activity, Translation translations, int time, List<Sensor> sensors) {
        this.id = id;
        this.activity = activity;
        this.translations = translations;
        this.time = time;
        this.sensors = sensors;
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Translation getTranslations() {
        return translations;
    }

    public void setTranslations(Translation translations) {
        this.translations = translations;
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

        // Get current language
        String lang = CurrentLang.getInstance().getLang();

        // Find activity in current language
        if (translations.getLang(lang) != null)
            return translations.getLang(lang);

        // Otherwise return default name
        return activity;
    }

}
