package com.derogab.adlanalyzer.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.models.FormElementsApiResponse;
import com.derogab.adlanalyzer.services.FormElementsService;
import com.derogab.adlanalyzer.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormElementsRepository {

    private static final String TAG = "FormElementsRepository";

    private static FormElementsRepository instance;
    private FormElementsService formElementsService;

    private FormElementsRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        formElementsService = retrofit.create(FormElementsService.class);
    }

    public static synchronized FormElementsRepository getInstance() {
        if (instance == null) {
            instance = new FormElementsRepository();
        }
        return instance;
    }

    public void getFormElements(MutableLiveData<List<FormElement>> elements) {

        Call<FormElementsApiResponse> call = formElementsService.getFormElementsResponse();

        call.enqueue(new Callback<FormElementsApiResponse>() {
            @Override
            public void onResponse(Call<FormElementsApiResponse> call, Response<FormElementsApiResponse> response) {

                Log.d(TAG, "Form elements downloaded...");

                elements.postValue(response.body().getElements());

            }

            @Override
            public void onFailure(Call<FormElementsApiResponse> call, Throwable t) {

                Log.e(TAG, "Error: " + t.getMessage());

            }
        });

    }

}
