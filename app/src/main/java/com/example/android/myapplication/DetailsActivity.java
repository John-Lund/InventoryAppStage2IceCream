package com.example.android.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myapplication.data.Constants;
import com.example.android.myapplication.data.InventoryContract.InventoryEntry;
import com.example.android.myapplication.data.Utilities;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentIceCreamUri;
    private ImageView mLargeIcon;
    private TextView mProductName;
    private TextView mDescriptionText;
    private TextView mStock;
    private TextView mPrice;
    private TextView mSupplierName;
    private TextView mSupplierPhone;
    private TextView mSupplierEmail;
    private TextView mSupplierAddress;
    private int mStockValue;
    private String mCurrentEmail;
    private String mSupplierPhoneNumber;
    // animation fields

    private ConstraintSet mConstraintSetStart = new ConstraintSet();
    private ConstraintSet mConstraintSetFinish = new ConstraintSet();
    private ConstraintLayout mConstraintLayout;

    private ChangeBounds mTransitionChangeBounds = new ChangeBounds();
    private int mTransitionSpeed = 100;
    private int mTransitionCloseSpeed = 0;
    private boolean mDeleteWarningIsVisible = false;
    private boolean mElegantCloseIsOn = false;
    // delete confirmation fields
    private Button mDeleteCancel;
    private Button mDeleteConfirm;
    private TextView mDeleteMessage;
    private boolean mDeleteIsConfirmed = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;

        setContentView(R.layout.activity_details_start);
        mConstraintLayout = findViewById(R.id.details_start_layout);
        mConstraintSetStart.clone(mConstraintLayout);
        mConstraintSetFinish.clone(context, R.layout.activity_details_finish);

        // adding listener to scrim to prevent events falling through to lower views
        ImageView scrim = findViewById(R.id.details_scrim_screen);
        scrim.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        // setting up toolbar
        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);

        // getting item Uri from intent
        Intent receivedIntent = getIntent();
        mCurrentIceCreamUri = receivedIntent.getData();

        // getting views to populate with data once load is finished
        mLargeIcon = findViewById(R.id.details_large_icon_imageview);
        mProductName = findViewById(R.id.details_product_name_textview);
        mDescriptionText = findViewById(R.id.details_product_description_textview);
        ImageButton plusButton = findViewById(R.id.details_plus_button);
        ImageButton minusButton = findViewById(R.id.details_minus_button);
        mStock = findViewById(R.id.details_in_stock_textview);
        mPrice = findViewById(R.id.details_price_per_unit_textview);
        mSupplierName = findViewById(R.id.details_supplier_name_textview);
        mSupplierPhone = findViewById(R.id.details_suppplier_tel_number_textview);
        mSupplierEmail = findViewById(R.id.details_supplier_email_textview);
        mSupplierAddress = findViewById(R.id.details_supplier_address_textview);

        // getting references to UI items and setting some initial visibility states
        mDeleteCancel = findViewById(R.id.details_delete_cancel_button);
        mDeleteConfirm = findViewById(R.id.details_delete_confirm_button);
        mDeleteMessage = findViewById(R.id.details_delete_message);
        mDeleteCancel.setVisibility(View.GONE);
        mDeleteConfirm.setVisibility(View.GONE);
        mDeleteMessage.setVisibility(View.GONE);
        ImageButton backButton = findViewById(R.id.details_back_button);
        ImageButton editButton = findViewById(R.id.details_edit_button);
        Button deleteButton = findViewById(R.id.details_delete_button);
        ImageButton emailButton = findViewById(R.id.details_supplier_email_icon);
        ImageButton phoneButton = findViewById(R.id.details_supplier_tel_icon);

        // setting up animation components

        mTransitionChangeBounds.setInterpolator(new OvershootInterpolator());
        mTransitionChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mDeleteCancel.setVisibility(View.GONE);
                mDeleteConfirm.setVisibility(View.GONE);
                mDeleteMessage.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (mDeleteWarningIsVisible) {
                    mDeleteCancel.setVisibility(View.VISIBLE);
                    mDeleteConfirm.setVisibility(View.VISIBLE);
                    mDeleteMessage.setVisibility(View.VISIBLE);
                }
                if (mElegantCloseIsOn) {
                    finish();
                }
                if (mDeleteIsConfirmed) {
                    deleteEntry();
                }

            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        // setting up onclick listener for UI items
        View.OnClickListener detailsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    case R.id.details_plus_button:
                        if (mStockValue < 99) {
                            mStockValue++;
                            updateStock();
                        }
                        break;
                    case R.id.details_minus_button:
                        if (mStockValue > 0) {
                            mStockValue--;
                            updateStock();
                        }
                        break;
                    case R.id.details_supplier_email_icon:
                        if (!TextUtils.isEmpty(mCurrentEmail)) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse(Constants.EMAIL_INTENT_STRING));
                            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mCurrentEmail});
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                        break;
                    case R.id.details_supplier_tel_icon:
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(Constants.TELEPHONE_INTENT_STRING + mSupplierPhoneNumber));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        break;
                    case R.id.details_delete_button:
                        showDeleteConfirmationLayout();
                        break;
                    case R.id.details_delete_cancel_button:
                        hideDeleteConfirmationLayout();
                        break;
                    case R.id.details_delete_confirm_button:
                        mDeleteIsConfirmed = true;
                        hideDeleteConfirmationLayout();
                        break;
                    case R.id.details_back_button:
                        finish();
                        break;
                    case R.id.details_edit_button:
                        Intent intent2 = new Intent(DetailsActivity.this, AddItemActivity.class);
                        intent2.setData(mCurrentIceCreamUri);
                        startActivity(intent2);
                        break;
                }
            }
        };
        // applying listeners to buttons
        plusButton.setOnClickListener(detailsListener);
        minusButton.setOnClickListener(detailsListener);
        phoneButton.setOnClickListener(detailsListener);
        emailButton.setOnClickListener(detailsListener);
        deleteButton.setOnClickListener(detailsListener);
        mDeleteCancel.setOnClickListener(detailsListener);
        mDeleteConfirm.setOnClickListener(detailsListener);
        backButton.setOnClickListener(detailsListener);
        editButton.setOnClickListener(detailsListener);
        // initialising loader
        getSupportLoaderManager().initLoader(1, null, this);
    }

    // methods to animate warning dialogue
    private void showDeleteConfirmationLayout() {
        mDeleteWarningIsVisible = true;
        mTransitionChangeBounds.setDuration(mTransitionSpeed);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mTransitionChangeBounds);
        mConstraintSetFinish.applyTo(mConstraintLayout);
   }

    private void hideDeleteConfirmationLayout() {
        mDeleteWarningIsVisible = false;
        mTransitionChangeBounds.setDuration(mTransitionCloseSpeed);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mTransitionChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    // delete item method
    private void deleteEntry() {
        if (mCurrentIceCreamUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentIceCreamUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.delete_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.delete_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    // override method to control back button
    @Override
    public void onBackPressed() {
        if (mDeleteWarningIsVisible) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    // method to update stock value in database
    private void updateStock() {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, mStockValue);
        String selection = InventoryEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentIceCreamUri))};
        getContentResolver().update(mCurrentIceCreamUri, values, selection, selectionArgs);
    }

    // loader overrides
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_ADDRESS,
                InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };
        String selection = InventoryEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentIceCreamUri))};
        CursorLoader loader = new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        data.moveToFirst();
        int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int descriptionColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
        int typeColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE);
        int supplierNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
        int supplierEmailColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        int supplierAddressColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_ADDRESS);

        String name = data.getString(nameColumnIndex);
        int quantity = data.getInt(quantityColumnIndex);
        String description = data.getString(descriptionColumnIndex);

        // getting price and reformatting to string
        double priceDouble = data.getDouble(priceColumnIndex);
        String priceString = Utilities.processPrice(priceDouble);

        // getting correct image id for ice cream type
        int type = data.getInt(typeColumnIndex);
        Utilities.ImageIds imageIds = Utilities.getImageIds(type);

        String supplierNameString = data.getString(supplierNameColumnIndex);
        String supplierPhoneString = data.getString(supplierPhoneColumnIndex);
        String supplierEmailString = data.getString(supplierEmailColumnIndex);
        String supplierAddressString = data.getString(supplierAddressColumnIndex);

        // updating UI
        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
        mProductName.setText(name);
        mDescriptionText.setText(description);
        mStock.setText(String.valueOf(quantity));
        mPrice.setText(priceString);
        mSupplierName.setText(supplierNameString);
        mSupplierPhone.setText(supplierPhoneString);
        mSupplierEmail.setText(supplierEmailString);
        mSupplierAddress.setText(supplierAddressString);

        // updating stock and email fields
        mStockValue = quantity;
        mCurrentEmail = supplierEmailString;
        mSupplierPhoneNumber = supplierPhoneString;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        loader = null;
    }
}
