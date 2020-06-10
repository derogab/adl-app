package com.derogab.adlanalyzer.models;

public class Translation {

    private String en;
    private String it;

    public Translation(String en, String it) {
        this.en = en;
        this.it = it;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getIt() {
        return it;
    }

    public void setIt(String it) {
        this.it = it;
    }

    public String getLang(String lang) {

        switch (lang) {
            case "en": return getEn();
            case "it": return getIt();
        }

        return getEn();

    }

    @Override
    public String toString() {
        return "Translation{" +
                "en='" + en + '\'' +
                ", it='" + it + '\'' +
                '}';
    }
}
