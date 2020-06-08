package com.derogab.adlanalyzer.services;

import com.derogab.adlanalyzer.models.FormTemplateApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FormTemplateService {

    @GET("form")
    Call<FormTemplateApiResponse> getFormTemplateResponse();

}
