package com.derogab.adlanalyzer.ui.personal;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.repositories.FormElementsRepository;

import java.util.List;

public class PersonalViewModel extends ViewModel {

    private final static String TAG = "PersonalViewModel";

    private MutableLiveData<List<FormElement>> elements;

    public LiveData<List<FormElement>> getFormElements() {

        if (elements == null) {
            elements = new MutableLiveData<List<FormElement>>();

            Log.d(TAG, "Download elements...");

            FormElementsRepository.getInstance().getFormElements(elements);

        }

        Log.d(TAG, "Get elements...");
        return elements;

    }

}
