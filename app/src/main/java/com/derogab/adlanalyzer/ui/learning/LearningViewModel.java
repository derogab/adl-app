package com.derogab.adlanalyzer.ui.learning;

import android.util.Log;

import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.repositories.ActivitiesRepository;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LearningViewModel extends ViewModel {

    private final static String TAG = "LearningViewModel";

    private MutableLiveData<List<Activity>> activities;

    public LiveData<List<Activity>> getActivities() {

        if (activities == null) {
            activities = new MutableLiveData<List<Activity>>();

            Log.d(TAG, "Download activities...");

            ActivitiesRepository.getInstance().getActivities(activities);

        }

        Log.d(TAG, "Get activities...");
        return activities;

    }

}
