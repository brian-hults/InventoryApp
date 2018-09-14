package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.Locale;

public class ProductCursorAdapter extends CursorAdapter {
    // Constructor for a new ProductCursorAdapter
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // Creates a new blank list item view for products
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    // This method binds the product data (in the current row given by the cursor) to the
    // provided list item layout.
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find the list item fields to populate
        TextView productNameView = view.findViewById(R.id.catalog_name_view);
        TextView productPriceView = view.findViewById(R.id.catalog_price_view);
        final TextView productQuantityView = view.findViewById(R.id.catalog_quantity_text);

        // Extract properties from cursor
        String productName = cursor.getString(cursor.getColumnIndexOrThrow
                (InventoryEntry.COLUMN_PRODUCT_NAME));
        double productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow
                (InventoryEntry.COLUMN_PRODUCT_PRICE));
        final int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow
                (InventoryEntry.COLUMN_PRODUCT_QUANTITY));

        // Convert price and quantity values to Strings for setText
        String priceText = String.format(Locale.US, "%.2f", productPrice);
        String quantityText = String.valueOf(productQuantity);


        // Populate list item fields with extracted properties
        productNameView.setText(productName);
        productPriceView.setText(priceText);
        productQuantityView.setText(quantityText);

        Button saleButton = view.findViewById(R.id.sale_button);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(productQuantityView.getText().toString());
                if (quantity == 0) {
                    Toast.makeText(context, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                } else {
                    quantity -= 1;
                    onClickUpdateProduct(quantity, context, cursor);
                    String quantityText = String.valueOf(quantity);
                    productQuantityView.setText(quantityText);
                }
            }
        });
    }

    /**
     * This is a helper method that takes in the updated quantity value after clicking the 'SALE'
     * button and updates the quantity of that product in the database.
     */
    private void onClickUpdateProduct(int quantity, Context context, Cursor cursor) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri currentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        int rowsAffected = context.getContentResolver().update(currentProductUri, values,
                null, null);
        if (rowsAffected == 0) {
            Toast.makeText(context, "Quantity Update Failed",
                            Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Quantity Updated",
                            Toast.LENGTH_SHORT).show();
        }
    }
}
