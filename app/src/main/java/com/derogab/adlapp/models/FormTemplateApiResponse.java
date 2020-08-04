package com.derogab.adlapp.models;

import java.util.List;

public class FormTemplateApiResponse {

    private String status;
    private List<FormGroup> groups;

    public FormTemplateApiResponse(String status, List<FormGroup> groups) {
        this.status = status;
        this.groups = groups;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FormGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<FormGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "FormElementsApiResponse{" +
                "status='" + status + '\'' +
                ", groups=" + groups +
                '}';
    }
}
