package com.rezapramudhika.simplepos.rest;

import com.rezapramudhika.simplepos.model.LoginBody;
import com.rezapramudhika.simplepos.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiInterface {
    @POST("/login")
    Call<LoginResponse> login(@Body LoginBody LoginBody);
}