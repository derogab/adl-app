package com.derogab.adlapp.services;

import com.derogab.adlapp.models.ActivitiesApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ActivitiesService {

    @GET("activities")
    Call<ActivitiesApiResponse> getActivitiesResponse();

}
