package com.rezapramudhika.simplepos.model;

import com.google.gson.annotations.SerializedName;

public class TransactionMenu {

    @SerializedName("id")
    private int id;
    @SerializedName("TransactionId")
    private int transactionId;
    @SerializedName("MenuId")
    private int menuId;
    @SerializedName("qty")
    private int qty;

    public static final String TABLE_NAME = "transaction_menu";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TRANSACTION_ID = "transactionId";
    public static final String COLUMN_MENU_ID = "menuId";
    public static final String COLUMN_QTY = "qty";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TRANSACTION_ID + " INTEGER,"
                    + COLUMN_MENU_ID + " INTEGER,"
                    + COLUMN_QTY + " INTEGER"
                    + ")";

    public TransactionMenu () {

    }

    public TransactionMenu(int id, int transactionId, int menuId, int qty) {
        this.id = id;
        this.transactionId = transactionId;
        this.menuId = menuId;
        this.qty = qty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
