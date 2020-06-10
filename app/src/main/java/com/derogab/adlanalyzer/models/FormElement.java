package com.derogab.adlanalyzer.models;

import java.util.List;

public class FormElement {

    private String id;
    private String type;
    private String text;
    private Translation translations;
    private List<FormSubElement> contents;
    private List<Option> options;

    public FormElement(String id, String type, String text, Translation translations, List<FormSubElement> contents, List<Option> options) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.translations = translations;
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

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public Translation getTranslations() {
        return translations;
    }

    public void setTranslations(Translation translations) {
        this.translations = translations;
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

        if (options != null)
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
                ", text='" + text + '\'' +
                ", translations=" + translations +
                ", contents=" + contents +
                ", options=" + options +
                '}';
    }

}
