package com.rezapramudhika.simplepos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.model.Category;
import com.rezapramudhika.simplepos.model.Menu;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
//    private List<Category> categories;
//    private List<Menu> menus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);

//        categories = db.getAllCategories();
//        menus = db.getMenus();
//
//        Toast.makeText(getApplicationContext(), "Category 0 = "+categories.get(0).getName(), Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(), "Menu 0 = "+menus.get(0).getName(), Toast.LENGTH_LONG).show();
    }
}
