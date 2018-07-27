package com.rezapramudhika.simplepos.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.model.CategoryResponse;
import com.rezapramudhika.simplepos.model.MenuResponse;
import com.rezapramudhika.simplepos.rest.ApiClient;
import com.rezapramudhika.simplepos.rest.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = LoadDataActivity.class.getSimpleName();
    private String token, restaurantId;
    private DatabaseHelper db;
    private Handler myHandler;
    private int userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        db = new DatabaseHelper(this);
        try {
            userId = new Memcache(getApplicationContext()).getUser().getId();
        } catch (Exception err) {
            userId = 0;
        }


        myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(userId != 0){
                    token = new Memcache(getApplicationContext()).getUser().getToken();
                    restaurantId = String.valueOf(new Memcache(getApplicationContext()).getRestaurant().getId());
                    db.emptyTable();
                    getCategories();
                }else {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 0);

    }

    private void getCategories() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<CategoryResponse> call = apiService.getCategories(token, restaurantId);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse>call, Response<CategoryResponse> response) {
                CategoryResponse categoryResponse = response.body();
                db.insertCategory(categoryResponse.getCategories());
                getMenus();
            }

            @Override
            public void onFailure(Call<CategoryResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });
    }

    private void getMenus() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<MenuResponse> call = apiService.getMenus(token, restaurantId);
        call.enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse>call, Response<MenuResponse> response) {
                MenuResponse menuResponse = response.body();
                db.insertMenu(menuResponse.getMenus());
                Intent intent = new Intent(SplashScreenActivity.this, CustomerListActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<MenuResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });
    }
}
