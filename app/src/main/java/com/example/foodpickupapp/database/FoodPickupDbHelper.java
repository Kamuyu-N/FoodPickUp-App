package com.example.foodpickupapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.foodpickupapp.database.DatabaseContract.UserEntry;
import com.example.foodpickupapp.util.PasswordUtils;

import com.example.foodpickupapp.database.DatabaseContract.RestaurantEntry;
import com.example.foodpickupapp.database.DatabaseContract.FoodItemEntry;
import com.example.foodpickupapp.database.DatabaseContract.OrderEntry;
import com.example.foodpickupapp.database.DatabaseContract.OrderItemEntry;

/**
 * SQLiteOpenHelper subclass that manages the FoodPickupApp database.
 * Creates all 5 tables on first launch and seeds the 3 restaurant locations.
 *
 * Tables created:
 * - restaurants (seeded with 3 cafes)
 * - users (FOOD-9)
 * - food_items (FOOD-10)
 * - orders (FOOD-17)
 * - order_items (FOOD-17)
 */
public class FoodPickupDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "FoodPickupDbHelper";

    // Singleton instance to prevent multiple database connections
    private static FoodPickupDbHelper instance;

    // -------------------------------------------------------------------------
    // SQL: Create the restaurants table
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_RESTAURANTS =
            "CREATE TABLE " + RestaurantEntry.TABLE_NAME + " (" +
                    RestaurantEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RestaurantEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    RestaurantEntry.COLUMN_LOCATION_CODE + " TEXT NOT NULL UNIQUE)";

    // -------------------------------------------------------------------------
    // SQL: Create the users table (FOOD-9)
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserEntry.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    UserEntry.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                    UserEntry.COLUMN_SALT + " TEXT NOT NULL, " +
                    UserEntry.COLUMN_ROLE + " TEXT NOT NULL DEFAULT 'STUDENT', " +
                    UserEntry.COLUMN_RESTAURANT_ID + " INTEGER, " +
                    UserEntry.COLUMN_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now')), " +
                    "FOREIGN KEY (" + UserEntry.COLUMN_RESTAURANT_ID + ") " +
                    "REFERENCES " + RestaurantEntry.TABLE_NAME + "(" + RestaurantEntry._ID + "))";

    // -------------------------------------------------------------------------
    // SQL: Create the food_items table (FOOD-10)
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_FOOD_ITEMS =
            "CREATE TABLE " + FoodItemEntry.TABLE_NAME + " (" +
                    FoodItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FoodItemEntry.COLUMN_RESTAURANT_ID + " INTEGER NOT NULL, " +
                    FoodItemEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    FoodItemEntry.COLUMN_DESCRIPTION + " TEXT, " +
                    FoodItemEntry.COLUMN_PRICE + " REAL NOT NULL, " +
                    FoodItemEntry.COLUMN_CATEGORY + " TEXT, " +
                    FoodItemEntry.COLUMN_IS_AVAILABLE + " INTEGER NOT NULL DEFAULT 1, " +
                    FoodItemEntry.COLUMN_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now')), " +
                    FoodItemEntry.COLUMN_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now')), " +
                    "FOREIGN KEY (" + FoodItemEntry.COLUMN_RESTAURANT_ID + ") " +
                    "REFERENCES " + RestaurantEntry.TABLE_NAME + "(" + RestaurantEntry._ID + "))";

    // -------------------------------------------------------------------------
    // SQL: Create the orders table (FOOD-17)
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_ORDERS =
            "CREATE TABLE " + OrderEntry.TABLE_NAME + " (" +
                    OrderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    OrderEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    OrderEntry.COLUMN_RESTAURANT_ID + " INTEGER NOT NULL, " +
                    OrderEntry.COLUMN_TOTAL_AMOUNT + " REAL NOT NULL, " +
                    OrderEntry.COLUMN_STATUS + " TEXT NOT NULL DEFAULT 'PLACED', " +
                    OrderEntry.COLUMN_PAYMENT_REFERENCE + " TEXT, " +
                    OrderEntry.COLUMN_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now')), " +
                    OrderEntry.COLUMN_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now')), " +
                    "FOREIGN KEY (" + OrderEntry.COLUMN_USER_ID + ") " +
                    "REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + "), " +
                    "FOREIGN KEY (" + OrderEntry.COLUMN_RESTAURANT_ID + ") " +
                    "REFERENCES " + RestaurantEntry.TABLE_NAME + "(" + RestaurantEntry._ID + "))";

    // -------------------------------------------------------------------------
    // SQL: Create the order_items table (FOOD-17)
    // -------------------------------------------------------------------------
    private static final String SQL_CREATE_ORDER_ITEMS =
            "CREATE TABLE " + OrderItemEntry.TABLE_NAME + " (" +
                    OrderItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    OrderItemEntry.COLUMN_ORDER_ID + " INTEGER NOT NULL, " +
                    OrderItemEntry.COLUMN_FOOD_ITEM_ID + " INTEGER NOT NULL, " +
                    OrderItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    OrderItemEntry.COLUMN_PRICE_AT_PURCHASE + " REAL NOT NULL, " +
                    "FOREIGN KEY (" + OrderItemEntry.COLUMN_ORDER_ID + ") " +
                    "REFERENCES " + OrderEntry.TABLE_NAME + "(" + OrderEntry._ID + "), " +
                    "FOREIGN KEY (" + OrderItemEntry.COLUMN_FOOD_ITEM_ID + ") " +
                    "REFERENCES " + FoodItemEntry.TABLE_NAME + "(" + FoodItemEntry._ID + "))";

    // -------------------------------------------------------------------------
    // SQL: Drop tables (for onUpgrade during development)
    // -------------------------------------------------------------------------
    private static final String SQL_DROP_ORDER_ITEMS = "DROP TABLE IF EXISTS " + OrderItemEntry.TABLE_NAME;
    private static final String SQL_DROP_ORDERS = "DROP TABLE IF EXISTS " + OrderEntry.TABLE_NAME;
    private static final String SQL_DROP_FOOD_ITEMS = "DROP TABLE IF EXISTS " + FoodItemEntry.TABLE_NAME;
    private static final String SQL_DROP_USERS = "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    private static final String SQL_DROP_RESTAURANTS = "DROP TABLE IF EXISTS " + RestaurantEntry.TABLE_NAME;

    /**
     * Private constructor — use getInstance() to get the singleton.
     */
    private FoodPickupDbHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    /**
     * Returns the singleton instance of the database helper.
     * This prevents multiple database connections from being opened simultaneously.
     */
    public static synchronized FoodPickupDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FoodPickupDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Create tables in order of dependencies (restaurants first, then users, etc.)
        db.execSQL(SQL_CREATE_RESTAURANTS);
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_FOOD_ITEMS);
        db.execSQL(SQL_CREATE_ORDERS);
        db.execSQL(SQL_CREATE_ORDER_ITEMS);

        // Seed the 3 restaurant locations
        seedRestaurants(db);

        // Seed the default admin account (FOOD-8)
        seedAdminUser(db);

        Log.d(TAG, "Database tables created and data seeded successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ". All existing data will be destroyed.");

        // Drop tables in reverse dependency order
        db.execSQL(SQL_DROP_ORDER_ITEMS);
        db.execSQL(SQL_DROP_ORDERS);
        db.execSQL(SQL_DROP_FOOD_ITEMS);
        db.execSQL(SQL_DROP_USERS);
        db.execSQL(SQL_DROP_RESTAURANTS);

        // Recreate everything
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraint enforcement
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
            Log.d(TAG, "Foreign key constraints enabled.");
        }
    }

    /**
     * Seeds the restaurants table with the 3 predefined cafe locations.
     * Called only once during initial database creation.
     */
    private void seedRestaurants(SQLiteDatabase db) {
        insertRestaurant(db, "Paul's Cafe", "PAULS_CAFE");
        insertRestaurant(db, "Sironi (Freida)", "SIRONI_FREIDA");
        insertRestaurant(db, "Sironi (SSHS)", "SIRONI_SSHS");
        Log.d(TAG, "Seeded 3 restaurant locations.");
    }

    /**
     * Helper method to insert a single restaurant record.
     */
    private void insertRestaurant(SQLiteDatabase db, String name, String locationCode) {
        ContentValues values = new ContentValues();
        values.put(RestaurantEntry.COLUMN_NAME, name);
        values.put(RestaurantEntry.COLUMN_LOCATION_CODE, locationCode);
        db.insert(RestaurantEntry.TABLE_NAME, null, values);
    }

    /**
     * Seeds a default admin account so the admin features (FOOD-8) are usable on first launch.
     * Credentials: admin@foodpickup.edu / Admin123!
     * Called only once during initial database creation.
     */
    private void seedAdminUser(SQLiteDatabase db) {
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword("Admin123!", salt);

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_EMAIL, "admin@foodpickup.edu");
        values.put(UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        values.put(UserEntry.COLUMN_SALT, salt);
        values.put(UserEntry.COLUMN_ROLE, "ADMIN");
        db.insert(UserEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Seeded default admin account (admin@foodpickup.edu).");
    }
}
