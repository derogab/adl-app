package com.derogab.adlanalyzer.ui.analyzer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnalyzerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AnalyzerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is analyzer fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}