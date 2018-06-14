package com.example.android.myapplication.data;


import android.util.Log;

import com.example.android.myapplication.R;

public final class Utilities {

    // class for storing image information
    public static class ImageIds {
        private int mIconSmallId;
        private int mIconLargeId;
        private String mProductTypeString;

        public ImageIds(int iconSmallId, int iconLargeId, String productTypeString) {
            this.mIconSmallId = iconSmallId;
            this.mIconLargeId = iconLargeId;
            this.mProductTypeString = productTypeString;
        }

        public int getmIconSmallId() {
            return mIconSmallId;
        }

        public int getmIconLargeId() {
            return mIconLargeId;
        }

        public String getmProductTypeString() {
            return mProductTypeString;
        }
    }

    // method to return ImageIds object with correct data
    public static ImageIds getImageIds(int productType) {
        String productTypeString;
        int iconSmallId;
        int iconLargeId;
        switch (productType) {
            case 1:
                iconSmallId = R.drawable.svg_icon_cone;
                iconLargeId = R.drawable.svg_icon_cone_white_circle;
                productTypeString = "CONE";
                break;
            case 2:
                iconSmallId = R.drawable.svg_icon_sandwich;
                iconLargeId = R.drawable.svg_icon_sandwich_white_circle;
                productTypeString = "SANDWICH";
                break;
            case 3:
                iconSmallId = R.drawable.svg_lolly_with_rectangle_as_path;
                iconLargeId = R.drawable.svg_lolly_white_circle_new;
                productTypeString = "LOLLY";
                break;
            case 4:
                iconSmallId = R.drawable.svg_stick_new_path;
                iconLargeId = R.drawable.svg_stick_white_circle_new;
                productTypeString = "STICK";
                break;
            default:
                iconSmallId = R.drawable.svg_icon_tub;
                iconLargeId = R.drawable.svg_icon_tub_white_circle;
                productTypeString = "TUB";
                break;
        }
        return new ImageIds(iconSmallId, iconLargeId, productTypeString);
    }

    // method to put price strings into the correct format
    public static String processPrice(double price) {
        String priceString = String.valueOf(price);
        if (priceString.length() == 3) {
            priceString = priceString + "0";
        }
        if (priceString.length() == 4 && priceString.contains(".")) {
            int indexOfDecimalPoint = priceString.indexOf(".");
            if (indexOfDecimalPoint == 2) {
                priceString = priceString + "0";
            }
        }
        return priceString;
    }
}

