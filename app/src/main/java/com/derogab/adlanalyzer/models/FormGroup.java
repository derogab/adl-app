package com.derogab.adlanalyzer.models;

import java.util.List;

public class FormGroup {

    private List<FormElement> elements;

    public FormGroup(List<FormElement> elements) {
        this.elements = elements;
    }

    public List<FormElement> getElements() {
        return elements;
    }

    public void setElements(List<FormElement> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "FormGroup{" +
                "elements=" + elements +
                '}';
    }
}
