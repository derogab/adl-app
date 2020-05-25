package com.derogab.adlanalyzer.services;

import com.derogab.adlanalyzer.models.FormElementsApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FormElementsService {

    @GET("xGFK8TvB")
    Call<FormElementsApiResponse> getFormElementsResponse();

}
