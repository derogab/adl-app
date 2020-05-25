package com.derogab.adlanalyzer.models;

import java.util.List;

public class FormElementsApiResponse {

    private String status;
    private List<FormElement> elements;

    public FormElementsApiResponse(String status, List<FormElement> elements) {
        this.status = status;
        this.elements = elements;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FormElement> getElements() {
        return elements;
    }

    public void setElements(List<FormElement> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "FormElementsApiResponse{" +
                "status='" + status + '\'' +
                ", elements=" + elements +
                '}';
    }

}
