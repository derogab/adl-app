package com.derogab.adlapp.services;

import com.derogab.adlapp.models.FormTemplateApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FormTemplateService {

    @GET("form")
    Call<FormTemplateApiResponse> getFormTemplateResponse();

}
