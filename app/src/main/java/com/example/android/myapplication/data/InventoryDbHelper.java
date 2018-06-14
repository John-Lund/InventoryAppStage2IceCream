package com.example.android.myapplication.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.myapplication.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";
    public static final Integer DATABASE_VERSION = 1;

    public static final String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "("
            + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT, "
            + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_ADDRESS + " TEXT, "
            + InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE + " INTEGER NOT NULL, "
            + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL );";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

























