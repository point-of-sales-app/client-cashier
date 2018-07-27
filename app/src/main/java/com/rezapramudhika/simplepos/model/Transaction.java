package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    private int id;
    @SerializedName("total")
    private int total;
    @SerializedName("PaymentMethodId")
    private int paymentMethodId;
    @SerializedName("RestaurantId")
    private int restaurantId;
    @SerializedName("UserId")
    private int userId;

    private int tableNumber;

    public static final String TABLE_NAME = "transactions";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_PAYMENT_METHOD_ID = "paymentMethodId";
    public static final String COLUMN_RESTAURANT_ID = "menuId";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_TABLE_NUMBER = "tableNumber";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TOTAL + " INTEGER,"
                    + COLUMN_PAYMENT_METHOD_ID + " INTEGER,"
                    + COLUMN_RESTAURANT_ID + " INTEGER,"
                    + COLUMN_USER_ID + " INTEGER,"
                    + COLUMN_TABLE_NUMBER + " INTEGER"
                    + ")";

    public Transaction() {
    }

    public Transaction(int id, int total, int paymentMethodId, int restaurantId, int userId, int tableNumber) {
        this.id = id;
        this.total = total;
        this.paymentMethodId = paymentMethodId;
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.tableNumber = tableNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
}
