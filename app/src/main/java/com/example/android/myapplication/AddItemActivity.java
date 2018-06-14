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
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myapplication.data.Constants;
import com.example.android.myapplication.data.InventoryContract.InventoryEntry;
import com.example.android.myapplication.data.Utilities;

public class AddItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // UI fields
    private ImageView mLargeIcon;
    private EditText mProductNameEditText;
    private EditText mProductDescriptionEditText;
    private EditText mQuantityInStockEditText;
    private EditText mPriceEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mSupplierEmailEditText;
    private EditText mSupplierAddressEditText;
    private Button mNoButton;
    private Button mYesButton;
    private TextView mWarningMessage;
    private ImageButton mImageSelectIcon;
    // programming logic fields
    private boolean mEditModeIsOn;
    private Uri mCurrentIceCreamUri;
    private boolean mEntryHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mEntryHasChanged = true;
            return false;
        }
    };
    private int mCurrentProductType;

    // animation fields
    private ConstraintLayout mMain_layout;
    private ChangeBounds mWarningChangeBounds = new ChangeBounds();
    private ChangeBounds mIconSelectChangeBounds = new ChangeBounds();
   // private ConstraintSet mConstraintSet = new ConstraintSet();

    private ConstraintSet mConstraintSetStart = new ConstraintSet();
    private ConstraintSet mConstraintSetFinish = new ConstraintSet();
    private ConstraintSet mConstraintSetOptions = new ConstraintSet();
    private ConstraintLayout mConstraintLayout;


    private int mTransitionCloseSpeed = 0;
    private int mTransitionSpeed = 100;
    private boolean mIsQuitWarningVisible = false;
    private boolean mIsElegantClose = false;
    private boolean mIconSelectLayoutIsVisible = false;
    private boolean mAnimationIsRunning = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_start);
        Context context = this;

//        mMain_layout = findViewById(R.id.add_item_start_constraints);
//        setContentView(mMain_layout);

        mConstraintLayout = findViewById(R.id.add_item_start_constraints);

        mConstraintSetStart.clone(mConstraintLayout);
        mConstraintSetFinish.clone(context, R.layout.activity_add_item_finish);
        mConstraintSetOptions.clone(context, R.layout.activity_add_item_type_option_finish);



        // getting references to UI items
        mImageSelectIcon = findViewById(R.id.add_item_image_select_icon);
        mYesButton = findViewById(R.id.add_item_yes_button);
        mNoButton = findViewById(R.id.add_item_no_button);
        mWarningMessage = findViewById(R.id.add_item_warning_message);
        ImageButton coneButton = findViewById(R.id.add_item_cone_select_button);
        ImageButton sandwichButton = findViewById(R.id.add_item_sandwich_select_button);
        ImageButton tubButton = findViewById(R.id.add_item_tub_select_button);
        ImageButton lollyButton = findViewById(R.id.add_item_lolly_select_button);
        ImageButton StickButton = findViewById(R.id.add_item_stick_select_button);
        mProductNameEditText = findViewById(R.id.add_item_product_name_edittext);
        mProductDescriptionEditText = findViewById(R.id.add_item_product_descriptionEdittext);
        mQuantityInStockEditText = findViewById(R.id.add_item_in_stock_edittext);
        mPriceEditText = findViewById(R.id.add_item_unit_price_edittext);
        mSupplierNameEditText = findViewById(R.id.add_item_supplier_name_edittext);
        mSupplierPhoneEditText = findViewById(R.id.add_item_supplier_phonenumber_edittext);
        mSupplierEmailEditText = findViewById(R.id.add_item_supplier_emailaddress_edittext);
        mSupplierAddressEditText = findViewById(R.id.add_item_supplier_address_edittext);
        Button quitButton = findViewById(R.id.add_item_cancel_buttton);
        Button saveButton = findViewById(R.id.add_item_save_button);


        // checking intent and defining the activities behaviour accordingly
        mLargeIcon = findViewById(R.id.add_item_large_icon_imageview);
        Intent receivedIntent = getIntent();
        mCurrentIceCreamUri = receivedIntent.getData();
        if (!(mCurrentIceCreamUri == null)) {
            mEditModeIsOn = true;
            TextView title = findViewById(R.id.add_item_title);
            title.setText(R.string.edit_activity_title);
            mImageSelectIcon.setVisibility(View.VISIBLE);
            getSupportLoaderManager().initLoader(1, null, this);
        } else {
            mCurrentProductType = receivedIntent.getIntExtra(Constants.ICE_CREAM_TYPE, 0);
            Utilities.ImageIds imageIds = Utilities.getImageIds(mCurrentProductType);
            mLargeIcon.setImageResource(imageIds.getmIconSmallId());
            mImageSelectIcon.setVisibility(View.GONE);
        }

        // adding listener to scrim to prevent events falling through to lower views
        ImageView scrim = findViewById(R.id.add_item_scrim_screen);
        scrim.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mIconSelectLayoutIsVisible) {
                    hideIconSelectLayout();
                }
                return true;
            }
        });

        // setting up animation components

        mWarningChangeBounds.setInterpolator(new OvershootInterpolator());
        mWarningChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mYesButton.setVisibility(View.GONE);
                mNoButton.setVisibility(View.GONE);
                mWarningMessage.setVisibility(View.GONE);
                if(!mEditModeIsOn){
                    mImageSelectIcon.setVisibility(View.GONE);

                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (mIsQuitWarningVisible) {
                    mYesButton.setVisibility(View.VISIBLE);
                    mNoButton.setVisibility(View.VISIBLE);
                    mWarningMessage.setVisibility(View.VISIBLE);
                }
                if (mIsElegantClose) {
                    finish();
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
        mIconSelectChangeBounds.setDuration(mTransitionSpeed);
        mIconSelectChangeBounds.setInterpolator(new AccelerateDecelerateInterpolator());
        mIconSelectChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mAnimationIsRunning = true;
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mAnimationIsRunning = false;
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

        // setting listener on edit texts to detect possible changes by user
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductDescriptionEditText.setOnTouchListener(mTouchListener);
        mQuantityInStockEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mSupplierAddressEditText.setOnTouchListener(mTouchListener);


        // creating listener for buttons and defining actions
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.ImageIds imageIds;
                int id = v.getId();
                switch (id) {
                    case R.id.add_item_cancel_buttton:
                        if (mEntryHasChanged) {
                            showQuitConfirmationLayout();
                        } else {
                            finish();
                        }
                        break;
                    case R.id.add_item_save_button:
                        saveEntry();
                        break;
                    case R.id.add_item_yes_button:
                        mIsElegantClose = true;
                        hideQuitConfirmationLayout();
                        break;
                    case R.id.add_item_no_button:
                        hideQuitConfirmationLayout();
                        break;
                    case R.id.add_item_image_select_icon:
                        if (mAnimationIsRunning) {
                            return;
                        } else if (mIconSelectLayoutIsVisible) {
                            hideIconSelectLayout();
                        } else {
                            showIconSelectLayout();
                        }
                        break;
                    case R.id.add_item_cone_select_button:
                        mCurrentProductType = 1;
                        imageIds = Utilities.getImageIds(mCurrentProductType);
                        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
                        hideIconSelectLayout();
                        mEntryHasChanged = true;
                        break;
                    case R.id.add_item_sandwich_select_button:
                        mCurrentProductType = 2;
                        imageIds = Utilities.getImageIds(mCurrentProductType);
                        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
                        hideIconSelectLayout();
                        mEntryHasChanged = true;
                        break;
                    case R.id.add_item_lolly_select_button:
                        mCurrentProductType = 3;
                        imageIds = Utilities.getImageIds(mCurrentProductType);
                        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
                        hideIconSelectLayout();
                        mEntryHasChanged = true;
                        break;
                    case R.id.add_item_stick_select_button:
                        mCurrentProductType = 4;
                        imageIds = Utilities.getImageIds(mCurrentProductType);
                        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
                        hideIconSelectLayout();
                        mEntryHasChanged = true;
                        break;
                    case R.id.add_item_tub_select_button:
                        mCurrentProductType = 5;
                        imageIds = Utilities.getImageIds(mCurrentProductType);
                        mLargeIcon.setImageResource(imageIds.getmIconSmallId());
                        hideIconSelectLayout();
                        mEntryHasChanged = true;
                        break;
                }
            }
        };

        // applying listener to UI buttons
        coneButton.setOnClickListener(buttonListener);
        sandwichButton.setOnClickListener(buttonListener);
        tubButton.setOnClickListener(buttonListener);
        lollyButton.setOnClickListener(buttonListener);
        StickButton.setOnClickListener(buttonListener);
        saveButton.setOnClickListener(buttonListener);
        quitButton.setOnClickListener(buttonListener);
        mNoButton.setOnClickListener(buttonListener);
        mYesButton.setOnClickListener(buttonListener);
        mImageSelectIcon.setOnClickListener(buttonListener);
    }

    // method to show quit warning message
    private void showQuitConfirmationLayout() {
        mIsQuitWarningVisible = true;
        mWarningChangeBounds.setDuration(mTransitionSpeed);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mWarningChangeBounds);
        mConstraintSetFinish.applyTo(mConstraintLayout);
    }

    // method to hide quit warning message
    private void hideQuitConfirmationLayout() {
        mIsQuitWarningVisible = false;
        mWarningChangeBounds.setDuration(mTransitionCloseSpeed);
        TransitionManager.beginDelayedTransition(mConstraintLayout, mWarningChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    // method to show icon choice layout
    private void showIconSelectLayout() {
        mIconSelectLayoutIsVisible = true;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mIconSelectChangeBounds);
        mConstraintSetOptions.applyTo(mConstraintLayout);
    }

    // method to hide icon choice layout
    private void hideIconSelectLayout() {
        mIconSelectLayoutIsVisible = false;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mIconSelectChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    // override to control back button behaviour
    @Override
    public void onBackPressed() {
        if (mIsQuitWarningVisible) {
            return;
        } else if (mEntryHasChanged) {
            showQuitConfirmationLayout();
        } else if (mIconSelectLayoutIsVisible) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    // method to save entry to database
    private void saveEntry() {
        String name = mProductNameEditText.getText().toString().trim();
        String description = mProductDescriptionEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmail = mSupplierEmailEditText.getText().toString().trim();
        String supplierAddress = mSupplierAddressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.user_input_ic_name_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, R.string.user_input_ic_description_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        String stockString = mQuantityInStockEditText.getText().toString().trim();
        if (TextUtils.isEmpty(stockString)) {
            Toast.makeText(this, R.string.user_input_ic_stock_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        String priceString = (mPriceEditText.getText().toString().trim());
        if (TextUtils.isEmpty(priceString) || priceString.equals(".")) {
            Toast.makeText(this, R.string.user_input_ic_price_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        Double price = Double.valueOf(priceString);
        if (price > 99) {
            Toast.makeText(this, R.string.user_input_ic_excessive_price_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        int decimalPointPosition;
        if (priceString.contains(".")) {
            decimalPointPosition = priceString.indexOf(".");
            if (priceString.length() > decimalPointPosition + 3) {
                Toast.makeText(this, R.string.user_input_ic_price_format_warning, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, R.string.user_input_supplier_name_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, R.string.user_input_supplier_phone_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        supplierPhone = supplierPhone.replace(" ", "");
        for (int i = 0; i < supplierPhone.length(); i++) {
            if (!Character.isDigit(supplierPhone.charAt(i))) {
                Toast.makeText(this, R.string.user_input_supplier_phone_format_warning, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        supplierPhone = mSupplierPhoneEditText.getText().toString().trim();
        if (!(TextUtils.isEmpty(supplierEmail))) {
            if (!supplierEmail.contains("@")) {

                Toast.makeText(this, R.string.user_input_supplier_email_address_warning, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, Integer.valueOf(stockString));
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);

        if (!TextUtils.isEmpty(supplierEmail)) {
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmail);
        }
        if (!TextUtils.isEmpty(supplierAddress)) {
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_ADDRESS, supplierAddress);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE, mCurrentProductType);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhone);

        int rowsUpdated = 0;
        Uri uri = null;

        if (!mEditModeIsOn) {
            uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        } else {
            String selection = InventoryEntry._ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentIceCreamUri))};
            rowsUpdated = getContentResolver().update(mCurrentIceCreamUri, values, selection, selectionArgs);
        }

        if (uri == null && rowsUpdated == 0) {
            Toast.makeText(this, R.string.save_failed_message, Toast.LENGTH_SHORT).show();
        } else if (uri != null) {
            Toast.makeText(this, R.string.save_successful_message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.edit_successful_message, Toast.LENGTH_SHORT).show();
        }
        finish();
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
        mProductNameEditText.setText(name);
        mProductDescriptionEditText.setText(description);
        mQuantityInStockEditText.setText(String.valueOf(quantity));
        mPriceEditText.setText(priceString);
        mSupplierNameEditText.setText(supplierNameString);
        mSupplierPhoneEditText.setText(supplierPhoneString);
        mSupplierEmailEditText.setText(supplierEmailString);
        mSupplierAddressEditText.setText(supplierAddressString);
        mCurrentProductType = type;

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        loader = null;
    }
}
