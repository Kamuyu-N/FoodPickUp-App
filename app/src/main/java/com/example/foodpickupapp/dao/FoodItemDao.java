package com.example.foodpickupapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foodpickupapp.database.DatabaseContract.FoodItemEntry;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the FoodItems table.
 * Provides add, edit, and remove operations for menu items.
 *
 * Related to: FOOD-10 (staff can add, edit, or remove food items)
 */
public class FoodItemDao {

    private final FoodPickupDbHelper dbHelper;

    public FoodItemDao(FoodPickupDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Adds a new food item to the database.
     *
     * @param foodItem the food item to insert
     * @return the row ID of the newly inserted item, or -1 if an error occurred
     */
    public long insertFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FoodItemEntry.COLUMN_RESTAURANT_ID, foodItem.getRestaurantId());
        values.put(FoodItemEntry.COLUMN_NAME, foodItem.getName());
        values.put(FoodItemEntry.COLUMN_DESCRIPTION, foodItem.getDescription());
        values.put(FoodItemEntry.COLUMN_PRICE, foodItem.getPrice());
        values.put(FoodItemEntry.COLUMN_CATEGORY, foodItem.getCategory());
        values.put(FoodItemEntry.COLUMN_IS_AVAILABLE, foodItem.isAvailable() ? 1 : 0);

        return db.insert(FoodItemEntry.TABLE_NAME, null, values);
    }

    /**
     * Updates an existing food item in the database.
     *
     * @param foodItem the food item with updated fields
     * @return the number of rows affected
     */
    public int updateFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FoodItemEntry.COLUMN_NAME, foodItem.getName());
        values.put(FoodItemEntry.COLUMN_DESCRIPTION, foodItem.getDescription());
        values.put(FoodItemEntry.COLUMN_PRICE, foodItem.getPrice());
        values.put(FoodItemEntry.COLUMN_CATEGORY, foodItem.getCategory());
        values.put(FoodItemEntry.COLUMN_IS_AVAILABLE, foodItem.isAvailable() ? 1 : 0);
        values.put(FoodItemEntry.COLUMN_UPDATED_AT, "datetime('now')");

        String selection = FoodItemEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(foodItem.getId()) };

        return db.update(FoodItemEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Removes a food item from the database.
     *
     * @param foodItemId the ID of the food item to delete
     * @return the number of rows deleted
     */
    public int deleteFoodItem(long foodItemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = FoodItemEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(foodItemId) };

        return db.delete(FoodItemEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Retrieves all food items for a specific restaurant.
     * Items are ordered by category and then by name.
     *
     * @param restaurantId the restaurant to get items for
     * @return a list of food items for that restaurant
     */
    public List<FoodItem> getFoodItemsByRestaurant(long restaurantId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = FoodItemEntry.COLUMN_RESTAURANT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(restaurantId) };
        String orderBy = FoodItemEntry.COLUMN_CATEGORY + " ASC, " + FoodItemEntry.COLUMN_NAME + " ASC";

        Cursor cursor = db.query(
                FoodItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<FoodItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(cursorToFoodItem(cursor));
        }
        cursor.close();
        return items;
    }

    /**
     * Retrieves all available food items across all restaurants.
     * Items are ordered by category and then by name.
     *
     * Related to: FOOD-11 (student sees a list of available food with prices)
     *
     * @return a list of all available food items
     */
    public List<FoodItem> getAllAvailableItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = FoodItemEntry.COLUMN_IS_AVAILABLE + " = ?";
        String[] selectionArgs = { "1" };
        String orderBy = FoodItemEntry.COLUMN_CATEGORY + " ASC, " + FoodItemEntry.COLUMN_NAME + " ASC";

        Cursor cursor = db.query(
                FoodItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<FoodItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(cursorToFoodItem(cursor));
        }
        cursor.close();
        return items;
    }

    /**
     * Retrieves a single food item by its ID.
     *
     * @param id the food item ID
     * @return the FoodItem if found, or null
     */
    public FoodItem getFoodItemById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = FoodItemEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                FoodItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        FoodItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToFoodItem(cursor);
        }
        cursor.close();
        return item;
    }

    /**
     * Helper method to convert a database cursor row into a FoodItem object.
     */
    private FoodItem cursorToFoodItem(Cursor cursor) {
        FoodItem item = new FoodItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(FoodItemEntry._ID)));
        item.setRestaurantId(cursor.getLong(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_RESTAURANT_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_NAME)));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_DESCRIPTION)));
        item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_PRICE)));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_CATEGORY)));
        item.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_IS_AVAILABLE)) == 1);
        item.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_CREATED_AT)));
        item.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(FoodItemEntry.COLUMN_UPDATED_AT)));
        return item;
    }

    /**
     * Retrieves all available food items across all restaurants.
     * Items are ordered by category and then by name.
     *
     * @return a list of all available food items
     */
    public List<FoodItem> getAllAvailableItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = FoodItemEntry.COLUMN_IS_AVAILABLE + " = ?";
        String[] selectionArgs = { "1" };
        String orderBy = FoodItemEntry.COLUMN_CATEGORY + " ASC, " + FoodItemEntry.COLUMN_NAME + " ASC";

        Cursor cursor = db.query(
                FoodItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<FoodItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(cursorToFoodItem(cursor));
        }
        cursor.close();
        return items;
    }
}
