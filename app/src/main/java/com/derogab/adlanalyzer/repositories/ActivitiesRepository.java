package com.derogab.adlanalyzer.repositories;

import android.util.Log;

import com.derogab.adlanalyzer.models.ActivitiesApiResponse;
import com.derogab.adlanalyzer.models.Activity;
import com.derogab.adlanalyzer.services.ActivitiesService;
import com.derogab.adlanalyzer.utils.Constants;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivitiesRepository {

    private static final String TAG = "ActivitiesRepository";

    private static ActivitiesRepository instance;
    private ActivitiesService activitiesService;

    private ActivitiesRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(Constants.API_BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

        activitiesService = retrofit.create(ActivitiesService.class);
    }

    public static synchronized ActivitiesRepository getInstance() {
        if (instance == null) {
            instance = new ActivitiesRepository();
        }
        return instance;
    }

    public void getActivities(MutableLiveData<List<Activity>> activities) {

        Call<ActivitiesApiResponse> call = activitiesService.getActivitiesResponse();

        call.enqueue(new Callback<ActivitiesApiResponse>() {
            @Override
            public void onResponse(Call<ActivitiesApiResponse> call, Response<ActivitiesApiResponse> response) {

                Log.d(TAG, "Activities downloaded...");

                activities.postValue(response.body().getActivities());

            }

            @Override
            public void onFailure(Call<ActivitiesApiResponse> call, Throwable t) {

                Log.e(TAG, "Error: " + t.getMessage());

            }
        });

    }


}
