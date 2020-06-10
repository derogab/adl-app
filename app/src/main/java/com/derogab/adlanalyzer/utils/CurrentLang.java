package com.derogab.adlanalyzer.utils;

public class CurrentLang {

    private String lang;
    private static CurrentLang instance;

    private CurrentLang() { }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public static synchronized CurrentLang getInstance() {

        if (instance == null) {
            instance = new CurrentLang();
        }

        return instance;

    }
}
