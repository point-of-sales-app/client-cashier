package com.rezapramudhika.simplepos.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rezapramudhika.simplepos.MainActivity;
import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.model.LoginBody;
import com.rezapramudhika.simplepos.model.LoginResponse;
import com.rezapramudhika.simplepos.rest.ApiClient;
import com.rezapramudhika.simplepos.rest.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;
    private EditText email, password;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Mohon tunggu sebentar...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        email = findViewById(R.id.inputLoginEmail);
        password = findViewById(R.id.inputLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginBody loginBody = new LoginBody(email.getText().toString(), password.getText().toString());
                login(loginBody);
            }
        });
    }

    private void login (LoginBody loginBody) {
        mProgressDialog.show();
        btnLogin.setText("Tunggu sebentar...");
        btnLogin.setEnabled(false);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<LoginResponse> call = apiService.login(loginBody);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse>call, Response<LoginResponse> response) {
                mProgressDialog.dismiss();
                LoginResponse loginResponse = response.body();
                new Memcache(getApplicationContext()).setUser(loginResponse.getUser());
                new Memcache(getApplicationContext()).setRestaurant(loginResponse.getRestaurant());
                Intent intent = new Intent(LoginActivity.this, LoadDataActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<LoginResponse>call, Throwable t) {
                // Log error here since request failed
                mProgressDialog.dismiss();
                btnLogin.setText("Masuk");
                btnLogin.setEnabled(true);
                Log.e(TAG, t.toString());
            }
        });
    }

}
