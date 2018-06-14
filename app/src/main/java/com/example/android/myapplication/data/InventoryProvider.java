package com.example.android.myapplication.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.myapplication.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbhelper;
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private static final int ICECREAMS = 500;
    private static final int ICECREAM_ID = 555;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ICECREAMS, ICECREAMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ICECREAMS + "/#", ICECREAM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbhelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbhelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ICECREAM_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ICECREAM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return insertIceCream(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertIceCream(Uri uri, ContentValues values) {

        if (TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME))) {
            throw new IllegalArgumentException("Ice cream needs a name");
        }

        if (TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION))) {
            throw new IllegalArgumentException("Ice cream needs a description");
        }

        if (!(values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) >= 0
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) == null
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) > 99)) {
            throw new IllegalArgumentException("Ice cream quantity needs to be a positive integer between zero and 99 inclusive");
        }

        if (!(values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_PRICE) >= 0)
                || values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_PRICE) == null) {
            throw new IllegalArgumentException("Ice cream should have a price value (double greater than zero)");
        }
        if (TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME))) {
            throw new IllegalArgumentException("Supplier needs a name");
        }
        if (!(values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) > 0)
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) == null
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) > 5) {
            throw new IllegalArgumentException("Ice cream product type needs to be an integer between 1 and 5");
        }

        if (TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER))) {
            throw new IllegalArgumentException("Supplier needs a phone number");
        }

        String phoneNumber = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER).trim();

        phoneNumber = phoneNumber.replace(" ", "");

        for (int i = 0; i < phoneNumber.length(); i++) {
            if (!Character.isDigit(phoneNumber.charAt(i))) {
                throw new IllegalArgumentException("Supplier needs a valid phone number using digits 0 - 9 only");
            }
        }
        SQLiteDatabase database = mDbhelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mDbhelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case ICECREAMS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ICECREAM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Figure out if the URI matcher can match the URI to a specific code
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ICECREAMS:
                return updateIceCream(uri, values, selection, selectionArgs);
            case ICECREAM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateIceCream(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateIceCream(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME) && TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME))) {
            throw new IllegalArgumentException("Ice cream needs a name");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION) && TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION))) {
            throw new IllegalArgumentException("Ice cream needs a description");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)
                && (!(values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) >= 0)
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) == null
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY) > 99)) {
            throw new IllegalArgumentException("Ice cream quantity needs to be a positive integer between zero and 99 inclusive");
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE) && (!(values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_PRICE) >= 0)
                || values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_PRICE) == null)) {
            throw new IllegalArgumentException("Ice cream should have a price value (double greater than zero)");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME) && TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME))) {
            throw new IllegalArgumentException("Supplier needs a name");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) && (!(values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) > 0)
                || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) == null || values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE) > 5)) {
            throw new IllegalArgumentException("Ice cream product type needs to be an integer between 1 and 5");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER) && (TextUtils.isEmpty(values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)))) {

            throw new IllegalArgumentException("Supplier needs a phone number");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)) {
            String phoneNumber = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER).trim();

            phoneNumber = phoneNumber.replace(" ", "");
            for (int i = 0; i < phoneNumber.length(); i++) {
                if (!Character.isDigit(phoneNumber.charAt(i))) {
                    throw new IllegalArgumentException("Supplier needs a valid phone number");
                }
            }
        }

        SQLiteDatabase database = mDbhelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
