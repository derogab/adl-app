package com.derogab.adlanalyzer.services;

import com.derogab.adlanalyzer.models.ActivitiesApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ActivitiesService {

    @GET("activities")
    Call<ActivitiesApiResponse> getActivitiesResponse();

}
