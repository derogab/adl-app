package com.derogab.adlanalyzer.ui.analyzer;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.derogab.adlanalyzer.models.PhonePosition;
import com.derogab.adlanalyzer.repositories.PhonePositionsRepository;
import com.derogab.adlanalyzer.services.AnalyzerService;

import java.util.List;

public class AnalyzerViewModel extends ViewModel {

    private final static String TAG = "AnalyzerViewModel";

    private MutableLiveData<List<PhonePosition>> phonePositions;
    private int phonePositionSelectedIndex = 0;

    private boolean analyzingInProgress = false;

    private Intent service = null;

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
