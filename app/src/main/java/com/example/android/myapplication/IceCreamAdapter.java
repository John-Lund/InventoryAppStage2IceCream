package com.example.android.myapplication;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.myapplication.data.InventoryContract.InventoryEntry;
import com.example.android.myapplication.data.Utilities;

public class IceCreamAdapter extends CursorAdapter {

    private View.OnClickListener mButtonListener;
    private StringBuilder stringBuilder = new StringBuilder();
    public IceCreamAdapter(Context context, Cursor c, View.OnClickListener buttonListener) {
        super(context, c, 0);
        this.mButtonListener = buttonListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View cardView = LayoutInflater.from(context).inflate(R.layout.card_view_for_listview, parent, false);
        IceCreamViewHolder iceCreamViewHolder = new IceCreamViewHolder(cardView);
        cardView.setTag(iceCreamViewHolder);
        return cardView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        IceCreamViewHolder iceCreamViewHolder = (IceCreamViewHolder) view.getTag();

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
        int typeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRODUCT_TYPE);

        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);
        String description = cursor.getString(descriptionColumnIndex);

        int type = cursor.getInt(typeColumnIndex);
        double priceDouble = cursor.getDouble(priceColumnIndex);

        // populating view with current values
        iceCreamViewHolder.name.setText(name);
        iceCreamViewHolder.quantity.setText(String.valueOf(quantity));
        iceCreamViewHolder.description.setText(description);

        // getting image IDs and setting correct images
        Utilities.ImageIds imageIds = Utilities.getImageIds(type);
        iceCreamViewHolder.iconSmall.setImageResource(imageIds.getmIconSmallId());
        iceCreamViewHolder.iconLarge.setImageResource(imageIds.getmIconLargeId());
        iceCreamViewHolder.productType.setText(imageIds.getmProductTypeString());

        // converting price double into correctly formatted string
        String price = Utilities.processPrice(priceDouble);
        stringBuilder.append(context.getString(R.string.currency_for_listview)).append(" ").append(price);
        String formattedPrice = stringBuilder.toString();
        iceCreamViewHolder.price.setText(formattedPrice);
        stringBuilder.setLength(0);

        // applying listener to sale button and adding object to tag to carry necessary data
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        Integer id = cursor.getInt(idColumnIndex);
        SaleTagObject saleTagObject = new SaleTagObject(id, quantity);
        // applying listener to "sale" button
        iceCreamViewHolder.saleButton.setTag(saleTagObject);
        iceCreamViewHolder.saleButton.setOnClickListener(mButtonListener);

    }

    // view holder class
    static final class IceCreamViewHolder {
        TextView name;
        TextView price;
        TextView quantity;
        ImageView iconSmall;
        ImageView iconLarge;
        TextView description;
        TextView productType;
        Button saleButton;

        IceCreamViewHolder(View view) {
            name = view.findViewById(R.id.productNameTextView);
            price = view.findViewById(R.id.productPriceTextView);
            quantity = view.findViewById(R.id.productStockTextView);
            iconSmall = view.findViewById(R.id.productIconSmall);
            iconLarge = view.findViewById(R.id.productIconLarge);
            description = view.findViewById(R.id.productDescriptionTextView);
            productType = view.findViewById(R.id.productTypeTextView);
            saleButton = view.findViewById(R.id.saleButton);
        }
    }

    // class to create object with data to store in tags
    public static class SaleTagObject {
        private int mId;
        private int mQuantity;

        public SaleTagObject(int mId, int mQuantity) {
            this.mId = mId;
            this.mQuantity = mQuantity;
        }

        public int getId() {
            return mId;
        }

        public int getQuantity() {
            return mQuantity;
        }
    }
}
