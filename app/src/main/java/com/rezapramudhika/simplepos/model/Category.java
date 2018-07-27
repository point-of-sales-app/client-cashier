package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("RestaurantId")
    private int restaurantid;

    public static final String TABLE_NAME = "categories";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_RESTAURANT_ID = "restaurantId";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_RESTAURANT_ID + " INTEGER"
                    + ")";

    public Category() {

    }

    public Category(int id, String name, int restaurantid) {
        this.id = id;
        this.name = name;
        this.restaurantid = restaurantid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRestaurantid() {
        return restaurantid;
    }

    public void setRestaurantid(int restaurantid) {
        this.restaurantid = restaurantid;
    }
}
