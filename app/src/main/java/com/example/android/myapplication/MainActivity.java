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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myapplication.data.Constants;
import com.example.android.myapplication.data.InventoryContract.InventoryEntry;
import com.example.android.myapplication.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ChangeBounds mPopUpTransitionChangeBounds = new ChangeBounds();
    private ChangeBounds mOptionsTransitionChangeBounds = new ChangeBounds();
    private ConstraintSet mConstraintSetStart = new ConstraintSet();
    private ConstraintSet mConstraintSetFinish = new ConstraintSet();
    private ConstraintSet mConstraintSetOptions = new ConstraintSet();
    private ConstraintLayout mConstraintLayout;
    private FloatingActionButton mFab;
    private int mTransitionSpeed = 200;
    private boolean mIsPopUpMenuOpen = false;
    private IceCreamAdapter mIceCreamAdapter;
    private static final int ICE_CREAM_LOADER = 0;
    private boolean mIsAnimationRunning = false;
    private boolean mOptionsMenuIsOpen = false;
    private boolean mInsertingIsOn = false;
    private ViewGroup listFooter;
    private int mChosenIceCreamType;
    private boolean mStartAddItemActivity = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this;

        setContentView(R.layout.main_layout_start);

        // setting up constraint animation elements
        mConstraintLayout = findViewById(R.id.layout_start);
        mConstraintSetStart.clone(mConstraintLayout);
        mConstraintSetFinish.clone(context, R.layout.main_layout_finish);
        mConstraintSetOptions.clone(context, R.layout.main_layout_options_finish);

        // adding listener to scrims to prevent events falling through to lower views
        ImageView optionsScrim = findViewById(R.id.options_scrim);
        ImageView scrim = findViewById(R.id.scrim_screen);
        View.OnTouchListener scrimListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mIsAnimationRunning && mIsPopUpMenuOpen) {
                    closePopUpMenu();
                }
                return true;
            }
        };
        optionsScrim.setOnTouchListener(scrimListener);
        scrim.setOnTouchListener(scrimListener);

        // getting references to UI elements
        ImageView closeMenu = findViewById(R.id.select_type_imageview);
        mFab = findViewById(R.id.floatingActionButton2);
        Button coneButton = findViewById(R.id.cone_bttn);
        Button sandwichButton = findViewById(R.id.sandwich_bttn);
        Button lollyButton = findViewById(R.id.lolly_bttn);
        Button stickButton = findViewById(R.id.stick_bttn);
        Button tubButton = findViewById(R.id.tub_bttn);
        ImageButton optionsButton = findViewById(R.id.main_drop_down);

        // setting up listener for IU items
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    case R.id.floatingActionButton2:
                        openPopUpMenu();
                        break;
                    case R.id.select_type_imageview:
                        closePopUpMenu();
                        break;
                    case R.id.cone_bttn:
                        mChosenIceCreamType = 1;
                        mStartAddItemActivity = true;
                        closePopUpMenu();
                        break;
                    case R.id.sandwich_bttn:
                        mChosenIceCreamType = 2;
                        mStartAddItemActivity = true;
                        closePopUpMenu();
                        break;
                    case R.id.lolly_bttn:
                        mChosenIceCreamType = 3;
                        mStartAddItemActivity = true;
                        closePopUpMenu();
                        break;
                    case R.id.stick_bttn:
                        mChosenIceCreamType = 4;
                        mStartAddItemActivity = true;
                        closePopUpMenu();
                        break;
                    case R.id.tub_bttn:
                        mChosenIceCreamType = 5;
                        mStartAddItemActivity = true;
                        closePopUpMenu();
                        break;
                    case R.id.main_drop_down:
                        if (mOptionsMenuIsOpen) {
                            hideOptionsMenu();
                        } else {
                            showOptionsMenu();
                        }
                        break;
                    case R.id.options_close:
                        hideOptionsMenu();
                        break;
                    case R.id.options_delete_all:
                        hideOptionsMenu();
                        deleteAllEntries();
                        break;
                    case R.id.options_insert_demo_data:
                        mInsertingIsOn = true;
                        setUpDummyData();
                        break;
                }
            }
        };

        // applying listener to UI items
        closeMenu.setOnClickListener(buttonListener);
        mFab.setOnClickListener(buttonListener);
        coneButton.setOnClickListener(buttonListener);
        sandwichButton.setOnClickListener(buttonListener);
        lollyButton.setOnClickListener(buttonListener);
        stickButton.setOnClickListener(buttonListener);
        tubButton.setOnClickListener(buttonListener);
        optionsButton.setOnClickListener(buttonListener);

        // setting up animation components for fab button menu
        mPopUpTransitionChangeBounds.setDuration(mTransitionSpeed);
        mPopUpTransitionChangeBounds.setInterpolator(new OvershootInterpolator());
        mPopUpTransitionChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                mIsAnimationRunning = true;
                if (mIsPopUpMenuOpen) {
                    mFab.setVisibility(View.GONE);
                   }
                if (!mIsPopUpMenuOpen){
                    mFab.setVisibility(View.VISIBLE);
             }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mIsAnimationRunning = false;
                if (mStartAddItemActivity) {
                    mStartAddItemActivity = false;
                    openAddItemActivity(mChosenIceCreamType);
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
        // options menu set up
        final Button optionsDelete = findViewById(R.id.options_delete_all);
        final Button optionsInsertDemoData = findViewById(R.id.options_insert_demo_data);
        final ImageButton optionsCancel = findViewById(R.id.options_close);
        final TextView optionsCloseTextView = findViewById(R.id.options_close_textview);
        optionsCancel.setOnClickListener(buttonListener);
        optionsDelete.setOnClickListener(buttonListener);
        optionsInsertDemoData.setOnClickListener(buttonListener);

        mOptionsTransitionChangeBounds.setDuration(0);
        mOptionsTransitionChangeBounds.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                optionsDelete.setVisibility(View.GONE);
                optionsCancel.setVisibility(View.GONE);
                optionsInsertDemoData.setVisibility(View.GONE);
                optionsCloseTextView.setVisibility(View.GONE);
                if (mOptionsMenuIsOpen) {
                    mFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                if (mOptionsMenuIsOpen) {
                    optionsDelete.setVisibility(View.VISIBLE);
                    optionsCancel.setVisibility(View.VISIBLE);
                    optionsInsertDemoData.setVisibility(View.VISIBLE);
                    optionsCloseTextView.setVisibility(View.VISIBLE);

                    if (!mOptionsMenuIsOpen) {
                        mFab.setVisibility(View.VISIBLE);
                    }
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
        /// setting up list view, adapter and loader
        ListView listView = findViewById(R.id.list_view);
        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup listHeader = (ViewGroup) layoutInflater.inflate(R.layout.header_for_listview, listView, false);
        listFooter = (ViewGroup) layoutInflater.inflate(R.layout.footer_for_listview, listView, false);
        listView.addHeaderView(listHeader, null, false);
        listView.addFooterView(listFooter, null, false);

        // setting up listener to pass to adapter
        View.OnClickListener saleButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IceCreamAdapter.SaleTagObject saleTagObject =
                        (IceCreamAdapter.SaleTagObject) v.findViewById(R.id.saleButton).getTag();
                if (saleTagObject.getQuantity() > 0) {
                    updateStock(saleTagObject.getQuantity() - 1, saleTagObject.getId());
                }
            }
        };

        mIceCreamAdapter = new IceCreamAdapter(this, null, saleButtonListener);
        listView.setAdapter(mIceCreamAdapter);
        ImageView emptyView = findViewById(R.id.emptyDatabaseMessage);
        listView.setEmptyView(emptyView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        // initialising loader
        getSupportLoaderManager().initLoader(ICE_CREAM_LOADER, null, this);

    }

    // method to delete all entries in table
    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.entries_deleted_success,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.entries_deleted_nothing_to_delete_message,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // method to open Add Item activity
    private void openAddItemActivity(int typeOfIceCream) {
        Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
        intent.putExtra(Constants.ICE_CREAM_TYPE, typeOfIceCream);
        startActivity(intent);
    }

    // constraint layout animation methods
    private void openPopUpMenu() {
        mIsPopUpMenuOpen = true;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mPopUpTransitionChangeBounds);
        mConstraintSetFinish.applyTo(mConstraintLayout);
    }

    private void closePopUpMenu() {
        mIsPopUpMenuOpen = false;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mPopUpTransitionChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    private void showOptionsMenu() {
        mOptionsMenuIsOpen = true;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mOptionsTransitionChangeBounds);
        mConstraintSetOptions.applyTo(mConstraintLayout);
    }

    private void hideOptionsMenu() {
        mOptionsMenuIsOpen = false;
        TransitionManager.beginDelayedTransition(mConstraintLayout, mOptionsTransitionChangeBounds);
        mConstraintSetStart.applyTo(mConstraintLayout);
    }

    // method that updates stock value when user clicks "Sale"
    private void updateStock(int newStockValue, int rowId) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newStockValue);
        String selection = InventoryEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rowId)};
        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);
        getContentResolver().update(uri, values, selection, selectionArgs);
    }

    // loader override methods
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE,
        };
        CursorLoader loader = new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 2) {
            listFooter.setVisibility(View.VISIBLE);
        } else {
            listFooter.setVisibility(View.INVISIBLE);
        }
        if(mInsertingIsOn){
            hideOptionsMenu();
            mInsertingIsOn = false;
        }
        mIceCreamAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mIceCreamAdapter.swapCursor(null);
    }

    // method to add dummy data to database
    private void setUpDummyData() {
        ContentValues values;
        String[] names = {"Strawberry Cheesecake",
                "Apple And Blackcurrant Fab",
                "Maxibon Cookie",
                "Cornetto Chocolate Ice Cream",
                "Calippo Orange"
        };
        String[] description = {"Rich cheesecake flavoured ice cream in a tub.",
                "Blackcurrant and apple flavoured ice lolly with a topping of hundreds and thousands.",
                "Chocolate chip cookie flavoured ice cream in a biscuit sandwich dipped in chocolate and nuts.",
                "Delicious, crispy baked wafer, coated inside with a chocolate flavoured layer, combined with a " +
                        "delicious chocolate ice cream and dark chocolate pieces.",
                "Refreshing orange-flavoured stick in a pop-up tube."
        };
        int[] quantity = {8, 5, 14, 33, 21};
        double[] price = {2.50, 1.25, 0.90, 1.10, 3.20};
        String[] supplier = {"Haagen-Dazs", "Walls Ice Cream", "Nestle", "Walls Ice Cream", "Walls Ice Cream"};
        String[] email = {"tubs@haagendazs.com", "orders@walls.com", "sales@nestle.com", "orders@walls.com", "orders@walls.com"};
        String[] address = {"21 Stanley Place,\nNorthwood,\nTyneside,\nTS1 4JY.",
                "57 Walls Road,\nBarking,\nHertfordshire\nH12 4DA",
                "Unit 6,\nParkhurst Industrial Estate,\nWaverley\n Lincolnshire,\nLC1 3WN",
                "57 Walls Road,\nBarking,\nHertfordshire\nH12 4DA",
                "57 Walls Road,\nBarking,\nHertfordshire\nH12 4DA"
        };
        int[] type = {5, 3, 2, 1, 4};
        String[] phone = {"000 000 000 ", "0151 826 5282", "0172 4552 6383", "0151 826 5282", "0151 826 5282"};

        for (int i = 0; i < 5; i++) {
            values = new ContentValues();
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, names[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplier[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, email[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_ADDRESS, address[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE, type[i]);
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, phone[i]);
            getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        }
    }
}

