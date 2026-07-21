package com.example.foodpickupapp.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foodpickupapp.database.DatabaseContract.RestaurantEntry;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Restaurants table.
 * Provides read-only operations since restaurants are seeded at database creation.
 *
 * Related to: System Adaptation (multi-location architecture for 3 cafes)
 */
public class RestaurantDao {

    private final FoodPickupDbHelper dbHelper;

    public RestaurantDao(FoodPickupDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Retrieves all restaurant locations.
     *
     * @return a list of all restaurants (should contain 3 entries)
     */
    public List<Restaurant> getAllRestaurants() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                RestaurantEntry.TABLE_NAME,
                null,  // all columns
                null,  // no selection
                null,
                null, null,
                RestaurantEntry.COLUMN_NAME + " ASC"
        );

        List<Restaurant> restaurants = new ArrayList<>();
        while (cursor.moveToNext()) {
            restaurants.add(cursorToRestaurant(cursor));
        }
        cursor.close();
        return restaurants;
    }

    /**
     * Retrieves a single restaurant by its ID.
     *
     * @param id the restaurant ID
     * @return the Restaurant if found, or null
     */
    public Restaurant getRestaurantById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = RestaurantEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                RestaurantEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Restaurant restaurant = null;
        if (cursor.moveToFirst()) {
            restaurant = cursorToRestaurant(cursor);
        }
        cursor.close();
        return restaurant;
    }

    /**
     * Helper method to convert a database cursor row into a Restaurant object.
     */
    private Restaurant cursorToRestaurant(Cursor cursor) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(RestaurantEntry._ID)));
        restaurant.setName(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantEntry.COLUMN_NAME)));
        restaurant.setLocationCode(cursor.getString(cursor.getColumnIndexOrThrow(RestaurantEntry.COLUMN_LOCATION_CODE)));
        return restaurant;
    }
}
