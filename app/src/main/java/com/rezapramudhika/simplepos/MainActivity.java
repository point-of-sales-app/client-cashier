package com.rezapramudhika.simplepos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.rezapramudhika.simplepos.cache.Memcache;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "Restaurant = "+new Memcache(getApplicationContext()).getRestaurant().getName(), Toast.LENGTH_LONG).show();
    }
}
