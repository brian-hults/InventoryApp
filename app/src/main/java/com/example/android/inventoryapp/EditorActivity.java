package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.Locale;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // Initializes the LOG TAG for the activity
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Initialize a constant Cursor Loader ID
    public static final int CURSOR_LOADER_ID = 12;

    // Initialize a global variable to track the current product URI
    private Uri currentProductUri;

    // Initialize a global variable to track if the product database has changed
    private boolean productHasChanged;

    // EditText Field to enter the product name
    private EditText nameEditText;

    // EditText Field to enter the product price
    private EditText priceEditText;

    // TextView counter for product quantity
    private TextView quantityTextView;

    // EditText Field to enter product supplier name
    private EditText supplierNameEditText;

    // EditText Field to enter product supplier phone number
    private EditText supplierPhoneEditText;

    // Sets up the starting quantity value of 0. The value can only be positive, and
    // is controlled by the +/- buttons on the editor screen.
    private int quantity = 0;

    /** Sets up a touch listener that keeps track if an item has been clicked on */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // Set title of EditorActivity based on which situation we have.
        if (currentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            // Initialize a loader to put the pet data into the text views
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        }

        // Find all views to read user input from
        nameEditText = findViewById(R.id.edit_product_name);
        priceEditText = findViewById(R.id.edit_product_price);
        quantityTextView = findViewById(R.id.editor_quantity_view);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = findViewById(R.id.edit_supplier_phone);

        // Add OnTouchListeners to each edit view to track if they are clicked
        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);
    }

    /**
     * This method is called when the plus button is clicked.
     */
    public void increment(View view){
        quantity += 1;
        displayQuantity(quantity);
    }

    /**
     * This method is called when the minus button is clicked.
     */
    public void decrement(View view){
        if (quantity == 0) {
            quantity = 0;
            Toast.makeText(getApplicationContext(), getString(R.string.negative_quantity),
                    Toast.LENGTH_SHORT).show();
        } else {
            quantity -= 1;
        }
        displayQuantity(quantity);
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayQuantity(int productQuantity) {
        TextView quantityTextView = findViewById(R.id.editor_quantity_view);
        String quantityText = String.valueOf(productQuantity);
        quantityTextView.setText(quantityText);
    }

    /** Get user input from editor and save new product into database */
    private void saveProduct() {
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityTextView.getText().toString();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierPhoneString = supplierPhoneEditText.getText().toString().trim();

        // This section checks if the user clicked the Save button without updating any
        // of the fields in the EditorActivity and returns without saving if so.
        if (currentProductUri == null
                && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(supplierNameString)
                && TextUtils.isEmpty(supplierPhoneString))
        { return; }

        // This section checks to see if a pet weight was added with all of the other data.
        // If not, the pet weight is set to a default value of 0.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // Create a ContentValues object where columns are the keys
        // and pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhoneString);

        currentProductUri = getIntent().getData();

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Call the Delete Confirmation Dialog
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_call_supplier:
                // Send intent to call the supplier phone number
                callSupplier();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method to send an intent to call the supplier phone number
    public void callSupplier() {
        String[] projection = {
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE };

        Cursor cursor = getContentResolver().query(currentProductUri,projection,
                null,null,null);
        if (cursor.moveToFirst()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));
            Intent callIntent = new Intent(Intent.ACTION_DIAL,
                    Uri.fromParts("tel", phoneNumber, null));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, getString(R.string.call_failed_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE };

        // This section checks if the Current Product URI is null. If so,
        // it is replaced with the CONTENT_URI since a null URI cannot
        // be passed into the CursorLoader.
        Uri loadUri = InventoryContract.InventoryEntry.CONTENT_URI;
        if (currentProductUri != null) {
            loadUri = currentProductUri;
        }

        return new CursorLoader(
                this,
                loadUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            nameEditText.setText(data.getString(data.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_NAME)));

            Double price = data.getDouble(data.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_PRICE));
            String priceText = String.format(Locale.US, "%.2f", price);
            priceEditText.setText(priceText);

            Integer quantity = data.getInt(data.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_QUANTITY));
            String quantityText = String.valueOf(quantity);
            quantityTextView.setText(quantityText);

            supplierNameEditText.setText(data.getString(data.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)));
            supplierPhoneEditText.setText(data.getString(data.getColumnIndex
                    (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityTextView.setText("0");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            MenuItem menuItem2 = menu.findItem(R.id.action_call_supplier);
            menuItem.setVisible(false);
            menuItem2.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        int rowsDeleted = 0;
        if (currentProductUri != null) {
            rowsDeleted = getContentResolver().delete(currentProductUri,
                    null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }
}