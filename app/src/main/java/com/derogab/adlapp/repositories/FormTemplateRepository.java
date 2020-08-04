package com.derogab.adlapp.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.derogab.adlapp.models.FormTemplateApiResponse;
import com.derogab.adlapp.models.FormGroup;
import com.derogab.adlapp.services.FormTemplateService;
import com.derogab.adlapp.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormTemplateRepository {

    private static final String TAG = "FormTemplateRepository";

    private static FormTemplateRepository instance;
    private FormTemplateService formTemplateService;

    private FormTemplateRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        formTemplateService = retrofit.create(FormTemplateService.class);
    }

    public static synchronized FormTemplateRepository getInstance() {
        if (instance == null) {
            instance = new FormTemplateRepository();
        }
        return instance;
    }

    public void getFormTemplate(MutableLiveData<List<FormGroup>> elements) {

        Call<FormTemplateApiResponse> call = formTemplateService.getFormTemplateResponse();

        call.enqueue(new Callback<FormTemplateApiResponse>() {
            @Override
            public void onResponse(Call<FormTemplateApiResponse> call, Response<FormTemplateApiResponse> response) {

                Log.d(TAG, "Form template downloaded...");

                elements.postValue(response.body().getGroups());

            }

            @Override
            public void onFailure(Call<FormTemplateApiResponse> call, Throwable t) {

                Log.e(TAG, "Error: " + t.getMessage());

            }
        });

    }

}
