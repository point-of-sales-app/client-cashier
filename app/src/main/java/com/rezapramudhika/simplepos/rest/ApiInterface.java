package com.rezapramudhika.simplepos.rest;

import com.rezapramudhika.simplepos.model.CategoryResponse;
import com.rezapramudhika.simplepos.model.LoginBody;
import com.rezapramudhika.simplepos.model.LoginResponse;
import com.rezapramudhika.simplepos.model.MenuResponse;
import com.rezapramudhika.simplepos.model.TransactionBody;
import com.rezapramudhika.simplepos.model.TransactionMenu;
import com.rezapramudhika.simplepos.model.TransactionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiInterface {
    @POST("/login")
    Call<LoginResponse> login(
            @Body LoginBody LoginBody
    );

    @GET("/category/find")
    Call<CategoryResponse> getCategories(
            @Header("token") String token,
            @Query("restaurantid") String restaurantId
    );

    @GET("/menu/find")
    Call<MenuResponse> getMenus(
            @Header("token") String token,
            @Query("restaurantid") String restaurantId
    );

    @POST("/transaction/create")
    Call<TransactionResponse> postTransaction(
            @Header("token") String token,
            @Query("restaurantid") String restaurantId,
            @Body TransactionBody transactionBody
    );
}