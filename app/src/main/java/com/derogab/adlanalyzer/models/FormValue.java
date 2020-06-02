package com.derogab.adlanalyzer.models;

import java.util.List;

public class FormValue {

    private int key;
    private String value;
    private List<String> values;

    public FormValue(int key, String value) {
        this.key = key;
        this.value = value;
        this.values = null;
    }

    public FormValue(int key, List<String> values) {
        this.key = key;
        this.value = null;
        this.values = values;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.values = null;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.value = null;
        this.values = values;
    }

    @Override
    public String toString() {

        if (values != null)
            return "FormValue{" +
                    "key=" + key +
                    ", values=" + values +
                    '}';
        else
            return "FormValue{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';

    }
}
