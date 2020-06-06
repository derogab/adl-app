package com.derogab.adlanalyzer.ui.analyzer;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.derogab.adlanalyzer.models.PhonePosition;
import com.derogab.adlanalyzer.repositories.PhonePositionsRepository;

import java.util.List;

public class AnalyzerViewModel extends ViewModel {

    private final static String TAG = "AnalyzerViewModel";

    private MutableLiveData<List<PhonePosition>> phonePositions;
    private int phonePositionSelectedIndex = 0;

    private boolean analyzingInProgress = false;

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
}
