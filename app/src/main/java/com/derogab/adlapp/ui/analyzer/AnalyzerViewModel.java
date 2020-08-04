package com.derogab.adlapp.ui.analyzer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.derogab.adlapp.models.Activity;
import com.derogab.adlapp.models.PhonePosition;
import com.derogab.adlapp.repositories.ActivitiesRepository;
import com.derogab.adlapp.repositories.PhonePositionsRepository;
import com.derogab.adlapp.services.AnalyzerService;

import java.util.List;

public class AnalyzerViewModel extends ViewModel {

    private final static String TAG = "AnalyzerViewModel";

    private MutableLiveData<List<Activity>> activities;

    private MutableLiveData<List<PhonePosition>> phonePositions;
    private int phonePositionSelectedIndex = 0;

    private boolean analyzingInProgress = false;

    private String predictedActivity = null;

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

    public int getPhonePositionSelectedIndex() {
        return phonePositionSelectedIndex;
    }

    public void setPhonePositionSelectedIndex(int phonePositionSelectedIndex) {
        this.phonePositionSelectedIndex = phonePositionSelectedIndex;
    }

    public boolean isAnalyzingInProgress() {
        return analyzingInProgress;
    }

    public void setAnalyzingInProgress(boolean analyzingInProgress) {
        this.analyzingInProgress = analyzingInProgress;
    }

    public String getPredictedActivity() { return predictedActivity; }

    public void setPredictedActivity(String predictedActivity) {
        this.predictedActivity = predictedActivity;
    }

    public Intent getService(Context context) {

        if (service == null) {
            // Create service
            service = new Intent(context, AnalyzerService.class);
        }

        return service;
    }

    public void setService(Intent service) {
        this.service = service;
    }
}
