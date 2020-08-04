package com.derogab.adlapp.ui.learning;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.derogab.adlapp.models.Activity;
import com.derogab.adlapp.repositories.ActivitiesRepository;
import com.derogab.adlapp.models.PhonePosition;
import com.derogab.adlapp.repositories.PhonePositionsRepository;
import com.derogab.adlapp.services.LearningService;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LearningViewModel extends ViewModel {

    private final static String TAG = "LearningViewModel";

    private MutableLiveData<List<Activity>> activities;
    private int activitySelectedIndex = 0;

    private MutableLiveData<List<PhonePosition>> phonePositions;
    private int phonePositionSelectedIndex = 0;

    private boolean learningInProgress = false;

    private Intent service = null;

    public LiveData<List<Activity>> getActivities() {

        if (activities == null) {
            activities = new MutableLiveData<List<Activity>>();

            Log.d(TAG, "Download activities...");

            ActivitiesRepository.getInstance().getActivities(activities);

        }

        Log.d(TAG, "Get activities...");
        return activities;

    }

    public MutableLiveData<List<PhonePosition>> getPhonePositions(Context context) {

        if (phonePositions == null) {
            phonePositions = new MutableLiveData<List<PhonePosition>>();

            PhonePositionsRepository.getInstance(context).getPhonePositions(phonePositions);

        }

        return phonePositions;

    }

    public int getActivitySelectedIndex() {
        return activitySelectedIndex;
    }

    public void setActivitySelectedIndex(int activitySelectedIndex) {
        this.activitySelectedIndex = activitySelectedIndex;
    }

    public int getPhonePositionSelectedIndex() {
        return phonePositionSelectedIndex;
    }

    public void setPhonePositionSelectedIndex(int phonePositionSelectedIndex) {
        this.phonePositionSelectedIndex = phonePositionSelectedIndex;
    }

    public boolean isLearningInProgress() {
        return learningInProgress;
    }

    public void setLearningInProgress(boolean learningInProgress) {
        this.learningInProgress = learningInProgress;
    }

    public Intent getService(Context context) {

        if (service == null) {
            // Create service
            service = new Intent(context, LearningService.class);
        }

        return service;
    }

    public void setService(Intent service) {
        this.service = service;
    }
}
