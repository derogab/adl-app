package com.derogab.adlanalyzer.models;

public class FormSubElement {

    private String id;
    private String text;

    public FormSubElement(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SubFormElement{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
