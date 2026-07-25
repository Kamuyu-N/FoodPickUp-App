package com.example.foodpickupapp.database;

import android.provider.BaseColumns;

/**
 * Contract class that defines the database schema for the FoodPickupApp.
 * Contains inner classes for each table, defining column names and constants.
 *
 * Tables:
 * - Users: Stores user credentials and roles (FOOD-9)
 * - Restaurants: Stores the 3 cafe locations (System Adaptation)
 * - FoodItems: Stores menu items linked to restaurants (FOOD-10)
 * - Orders: Stores completed order headers (FOOD-17)
 * - OrderItems: Stores individual items within each order (FOOD-17)
 */
public final class DatabaseContract {

    // Database name and version
    public static final String DATABASE_NAME = "food_pickup.db";
    public static final int DATABASE_VERSION = 2;

    // Private constructor to prevent instantiation
    private DatabaseContract() {}

    // -------------------------------------------------------------------------
    // Users table — stores user credentials safely (FOOD-9)
    // -------------------------------------------------------------------------
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_SALT = "salt";
        public static final String COLUMN_ROLE = "role"; // STUDENT, STAFF, or ADMIN
        public static final String COLUMN_RESTAURANT_ID = "restaurant_id"; // FK, nullable (for STAFF)
        public static final String COLUMN_CREATED_AT = "created_at";
    }

    // -------------------------------------------------------------------------
    // Restaurants table — stores the 3 cafe locations (System Adaptation)
    // -------------------------------------------------------------------------
    public static class RestaurantEntry implements BaseColumns {
        public static final String TABLE_NAME = "restaurants";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LOCATION_CODE = "location_code";
    }

    // -------------------------------------------------------------------------
    // Food Items table — stores menu items per restaurant (FOOD-10)
    // -------------------------------------------------------------------------
    public static class FoodItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "food_items";
        public static final String COLUMN_RESTAURANT_ID = "restaurant_id"; // FK to restaurants
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_IS_AVAILABLE = "is_available"; // 1 = available, 0 = not
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    // -------------------------------------------------------------------------
    // Orders table — stores completed order headers (FOOD-17)
    // -------------------------------------------------------------------------
    public static class OrderEntry implements BaseColumns {
        public static final String TABLE_NAME = "orders";
        public static final String COLUMN_USER_ID = "user_id"; // FK to users
        public static final String COLUMN_RESTAURANT_ID = "restaurant_id"; // FK to restaurants
        public static final String COLUMN_TOTAL_AMOUNT = "total_amount";
        public static final String COLUMN_STATUS = "status"; // PLACED, PAID, PREPARING, READY, PICKED_UP
        public static final String COLUMN_PAYMENT_REFERENCE = "payment_reference";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    // -------------------------------------------------------------------------
    // Order Items table — stores individual items within each order (FOOD-17)
    // -------------------------------------------------------------------------
    public static class OrderItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "order_items";
        public static final String COLUMN_ORDER_ID = "order_id"; // FK to orders
        public static final String COLUMN_FOOD_ITEM_ID = "food_item_id"; // FK to food_items
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE_AT_PURCHASE = "price_at_purchase";
    }
}
