package com.derogab.adlapp.models;

import java.util.List;

public class ActivitiesApiResponse {

    private String status;
    private List<Activity> activities;

    public ActivitiesApiResponse(String status, List<Activity> activities) {
        this.status = status;
        this.activities = activities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return "ActivitiesApiResponse{" +
                "status='" + status + '\'' +
                ", activities=" + activities +
                '}';
    }
}
