package com.rezapramudhika.simplepos.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.model.Category;
import com.rezapramudhika.simplepos.model.Menu;
import com.rezapramudhika.simplepos.model.Transaction;
import com.rezapramudhika.simplepos.model.TransactionMenu;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "pos_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("--------------------------------------------");
        db.execSQL(Category.CREATE_TABLE);
        Log.d("TABEL CREATED = ", "Category");
        db.execSQL(Menu.CREATE_TABLE);
        Log.d("TABEL CREATED = ", "Menu");
        db.execSQL(Transaction.CREATE_TABLE);
        Log.d("TABEL CREATED = ", "Transaction");
        db.execSQL(TransactionMenu.CREATE_TABLE);
        Log.d("TABEL CREATED = ", "TransactionMenu");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Menu.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Transaction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TransactionMenu.TABLE_NAME);
        onCreate(db);
    }

    public void insertCategory(List<Category> category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (Category categoryItem : category) {
            values.put(Category.COLUMN_ID, categoryItem.getId());
            values.put(Category.COLUMN_NAME, categoryItem.getName());
            values.put(Category.COLUMN_RESTAURANT_ID, categoryItem.getRestaurantid());
            db.insert(Category.TABLE_NAME, null, values);
        }
        db.close();
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Category.TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndex(Category.COLUMN_ID)));
                category.setName(cursor.getString(cursor.getColumnIndex(Category.COLUMN_NAME)));
                category.setRestaurantid(cursor.getInt(cursor.getColumnIndex(Category.COLUMN_RESTAURANT_ID)));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        db.close();
        return categories;
    }

    public void insertMenu(List<Menu> menus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (Menu menu : menus) {
            values.put(Menu.COLUMN_ID, menu.getId());
            values.put(Menu.COLUMN_NAME, menu.getName());
            values.put(Menu.COLUMN_DESCRIPTION, menu.getDescription());
            values.put(Menu.COLUMN_PRICE, menu.getPrice());
            values.put(Menu.COLUMN_CATEGORY_ID, menu.getCategoryId());
            values.put(Menu.COLUMN_RESTAURANT_ID, menu.getRestaurantId());
            db.insert(Menu.TABLE_NAME, null, values);
        }
        db.close();
    }

    public List<Menu> getMenus(int id) {
        List<Menu> menus = new ArrayList<>();
        String selectQuery = "";
        if(id != 0){
            selectQuery = "SELECT * FROM " + Menu.TABLE_NAME + " WHERE "+ Menu.COLUMN_CATEGORY_ID +" = "+id+" ORDER BY "+Menu.COLUMN_CATEGORY_ID+" ASC";
        } else {
            selectQuery = "SELECT * FROM " + Menu.TABLE_NAME + " ORDER BY "+Menu.COLUMN_CATEGORY_ID+" ASC";
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Menu menu = new Menu();
                menu.setId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_ID)));
                menu.setName(cursor.getString(cursor.getColumnIndex(Menu.COLUMN_NAME)));
                menu.setPrice(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_PRICE)));
                menu.setDescription(cursor.getString(cursor.getColumnIndex(Menu.COLUMN_DESCRIPTION)));
                menu.setCategoryId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_CATEGORY_ID)));
                menu.setRestaurantId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_RESTAURANT_ID)));

                menus.add(menu);
            } while (cursor.moveToNext());
        }
        db.close();
        return menus;
    }

    public Menu getMenu (int id) {
        Menu menu = new Menu();
        String selectQuery = "";
        Log.d("GET MENU ID", ""+id);
        selectQuery = "SELECT * FROM " + Menu.TABLE_NAME + " WHERE "+ Menu.COLUMN_ID +" = "+id;

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    menu.setId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_ID)));
                    menu.setName(cursor.getString(cursor.getColumnIndex(Menu.COLUMN_NAME)));
                    menu.setPrice(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_PRICE)));
                    menu.setDescription(cursor.getString(cursor.getColumnIndex(Menu.COLUMN_DESCRIPTION)));
                    menu.setCategoryId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_CATEGORY_ID)));
                    menu.setRestaurantId(cursor.getInt(cursor.getColumnIndex(Menu.COLUMN_RESTAURANT_ID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("ERROR GET MENU", e.getMessage());
        }

        db.close();
        return menu;
    }

    public void insertTransaction(Context context, int tableNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Transaction.COLUMN_PAYMENT_METHOD_ID, 1);
        values.put(Transaction.COLUMN_RESTAURANT_ID, new Memcache(context).getRestaurant().getId());
        values.put(Transaction.COLUMN_USER_ID, new Memcache(context).getUser().getId());
        values.put(Transaction.COLUMN_TOTAL, 0);
        values.put(Transaction.COLUMN_TABLE_NUMBER, tableNumber);

        db.insert(Transaction.TABLE_NAME, null, values);
        db.close();
    }


    public void updateTransaction(int id, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Transaction.COLUMN_TOTAL, total);
        db.update(Transaction.TABLE_NAME, values, Transaction.COLUMN_ID+ " = " +id, null);
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Transaction.TABLE_NAME, Transaction.COLUMN_ID+ " = " +id, null);
        db.delete(TransactionMenu.TABLE_NAME, TransactionMenu.COLUMN_TRANSACTION_ID+ " = " +id, null);
    }

    public List<Transaction> getAllTransaction() {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Transaction.TABLE_NAME + " ORDER BY "+ Transaction.COLUMN_TABLE_NUMBER + " ASC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_ID)));
                transaction.setPaymentMethodId(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_PAYMENT_METHOD_ID)));
                transaction.setRestaurantId(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_RESTAURANT_ID)));
                transaction.setTotal(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_TOTAL)));
                transaction.setTableNumber(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_TABLE_NUMBER)));
                transaction.setUserId(cursor.getInt(cursor.getColumnIndex(Transaction.COLUMN_USER_ID)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        db.close();
        return transactions;
    }

    public List<TransactionMenu> getTransactionMenu(int id) {
        List<TransactionMenu> transactionMenus = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TransactionMenu.TABLE_NAME +" WHERE "+ TransactionMenu.COLUMN_TRANSACTION_ID +" = "+ id ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TransactionMenu transactionMenu = new TransactionMenu();
                transactionMenu.setId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_ID)));
                transactionMenu.setMenuId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_MENU_ID)));
                transactionMenu.setTransactionId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_TRANSACTION_ID)));
                transactionMenu.setQty(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_QTY)));
                transactionMenus.add(transactionMenu);
            } while (cursor.moveToNext());
        }
        db.close();
        return transactionMenus;
    }

    public void insertTransactionMenu(int transactionId, int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "";
        selectQuery = "SELECT * FROM " + TransactionMenu.TABLE_NAME + " WHERE "+ TransactionMenu.COLUMN_TRANSACTION_ID +" = "+ transactionId + " AND "+ TransactionMenu.COLUMN_MENU_ID +" = "+menuId;

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            Log.d("CURSOR COUNT", ""+cursor.getCount());
            if(cursor.getCount() == 0){
                ContentValues values = new ContentValues();
                values.put(TransactionMenu.COLUMN_MENU_ID, menuId);
                values.put(TransactionMenu.COLUMN_QTY, 1);
                values.put(TransactionMenu.COLUMN_TRANSACTION_ID, transactionId);
                db.insert(TransactionMenu.TABLE_NAME, null, values);
                Log.d("INSERT MENU", ""+menuId);
            } else {
                if (cursor.moveToFirst()) {
                    do {
                        TransactionMenu transactionMenu = new TransactionMenu();
                        transactionMenu.setId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_ID)));
                        transactionMenu.setQty(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_QTY)));
                        transactionMenu.setTransactionId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_TRANSACTION_ID)));
                        transactionMenu.setMenuId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_MENU_ID)));

                        ContentValues values = new ContentValues();
                        values.put(TransactionMenu.COLUMN_QTY, transactionMenu.getQty()+1);
                        db.update(TransactionMenu.TABLE_NAME, values, TransactionMenu.COLUMN_ID+" = "+transactionMenu.getId(), null);
                        Log.d("UPDATE MENU", ""+menuId);
                    } while (cursor.moveToNext());
                }
            }
        }catch (Exception e) {
            Log.e("ERROR INSERT MENU", e.getMessage());
        }
        db.close();
    }

    public void updateTransactionMenu(int transactionId, int menuId, int type) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "";
        selectQuery = "SELECT * FROM " + TransactionMenu.TABLE_NAME + " WHERE "+ TransactionMenu.COLUMN_TRANSACTION_ID +" = "+ transactionId + " AND "+ TransactionMenu.COLUMN_MENU_ID +" = "+menuId;

        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    TransactionMenu transactionMenu = new TransactionMenu();
                    transactionMenu.setId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_ID)));
                    transactionMenu.setQty(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_QTY)));
                    transactionMenu.setTransactionId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_TRANSACTION_ID)));
                    transactionMenu.setMenuId(cursor.getInt(cursor.getColumnIndex(TransactionMenu.COLUMN_MENU_ID)));

                    ContentValues values = new ContentValues();
                    if(type == 1){
                        values.put(TransactionMenu.COLUMN_QTY, transactionMenu.getQty()+1);
                    } else {
                        values.put(TransactionMenu.COLUMN_QTY, transactionMenu.getQty()-1);
                    }
                    db.update(TransactionMenu.TABLE_NAME, values, TransactionMenu.COLUMN_ID+" = "+transactionMenu.getId(), null);
                    Log.d("UPDATE MENU", ""+menuId);
                } while (cursor.moveToNext());
            }
        }catch (Exception e) {
            Log.e("ERROR INSERT MENU", e.getMessage());
        }
        db.close();
    }

    public void deleteTransactionMenu (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TransactionMenu.TABLE_NAME, TransactionMenu.COLUMN_ID + " = " + id, null);
    }

    public void emptyTable (){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Menu.TABLE_NAME, null, null);
        db.delete(Category.TABLE_NAME, null, null);
        db.close();
    }
}
