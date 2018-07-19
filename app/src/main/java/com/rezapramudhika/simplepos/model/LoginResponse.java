package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("msg")
    private String msg;
    @SerializedName("user")
    private User user;
    @SerializedName("restaurant")
    private Restaurant restaurant;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
