package com.rezapramudhika.simplepos.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.rezapramudhika.simplepos.model.Restaurant;
import com.rezapramudhika.simplepos.model.User;

public class Memcache {

    private String PREF_NAME = "POS";
    private SharedPreferences pref;
    private int PRIVATE_MODE = 0;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String USER_KEY = "user_key";
    private static final String RESTAURANT_KEY = "restaurant_key";

    public Memcache (Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setUser(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(USER_KEY, json);
        editor.commit();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = pref.getString(USER_KEY, "");
        return gson.fromJson(json, User.class);
    }

    public void setRestaurant(Restaurant restaurant) {
        Gson gson = new Gson();
        String json = gson.toJson(restaurant);
        editor.putString(RESTAURANT_KEY, json);
        editor.commit();
    }

    public Restaurant getRestaurant() {
        Gson gson = new Gson();
        String json = pref.getString(RESTAURANT_KEY, "");
        return gson.fromJson(json, Restaurant.class);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }


}
