package com.derogab.adlanalyzer.models;

import java.util.List;

public class FormElement {

    private String id;
    private String type;
    private List<FormSubElement> contents;
    private List<Option> options;

    public FormElement(String id, String type, List<FormSubElement> contents, List<Option> options) {
        this.id = id;
        this.type = type;
        this.contents = contents;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FormSubElement> getContents() {
        return contents;
    }

    public void setContents(List<FormSubElement> contents) {
        this.contents = contents;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getOption(String attribute) {

        List<Option> options = getOptions();

        for (int i = 0; i < options.size(); i++)
            if (options.get(i).getAttribute().equals(attribute))
                return options.get(i).getValue();

        return null;

    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "FormElement{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", contents=" + contents +
                ", options=" + options +
                '}';
    }
}
