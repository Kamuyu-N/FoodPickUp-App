package com.example.foodpickupapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foodpickupapp.database.DatabaseContract.UserEntry;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.User;
import com.example.foodpickupapp.util.PasswordUtils;

/**
 * Data Access Object for the Users table.
 * Provides CRUD operations for user credentials.
 *
 * Related to: FOOD-9 (database table to store user credentials safely)
 */
public class UserDao {

    private final FoodPickupDbHelper dbHelper;

    public UserDao(FoodPickupDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserts a new user into the database.
     * The password is hashed with a random salt before storage.
     *
     * @param email         the user's email address
     * @param plainPassword the plaintext password (will be hashed)
     * @param role          the user's role: "STUDENT", "STAFF", or "ADMIN"
     * @param restaurantId  the restaurant ID for staff, or -1 if not applicable
     * @return the row ID of the newly inserted user, or -1 if an error occurred
     */
    public long insertUser(String email, String plainPassword, String role, long restaurantId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Hash the password with a fresh salt
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(plainPassword, salt);

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_EMAIL, email);
        values.put(UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        values.put(UserEntry.COLUMN_SALT, salt);
        values.put(UserEntry.COLUMN_ROLE, role);

        // Only set restaurant_id if it's a valid value (for STAFF)
        if (restaurantId > 0) {
            values.put(UserEntry.COLUMN_RESTAURANT_ID, restaurantId);
        }

        return db.insert(UserEntry.TABLE_NAME, null, values);
    }

    /**
     * Retrieves a user by their email address.
     * Useful for login lookups.
     *
     * @param email the email to search for
     * @return the User object if found, or null if not found
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserEntry.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,  // all columns
                selection,
                selectionArgs,
                null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Retrieves a user by their primary key ID.
     *
     * @param id the user ID
     * @return the User object if found, or null if not found
     */
    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Updates an existing user's details (email, role, restaurant assignment).
     * Does NOT update the password — use updatePassword() for that.
     *
     * @param user the user with updated fields
     * @return the number of rows affected
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(UserEntry.COLUMN_ROLE, user.getRole());

        if (user.getRestaurantId() > 0) {
            values.put(UserEntry.COLUMN_RESTAURANT_ID, user.getRestaurantId());
        } else {
            values.putNull(UserEntry.COLUMN_RESTAURANT_ID);
        }

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(user.getId()) };

        return db.update(UserEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Deletes a user from the database.
     *
     * @param userId the ID of the user to delete
     * @return the number of rows deleted
     */
    public int deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        return db.delete(UserEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Verifies a user's password against the stored hash.
     * Used during login authentication.
     *
     * @param email         the user's email
     * @param plainPassword the plaintext password to verify
     * @return true if the credentials are valid, false otherwise
     *
     * TODO: This will be used by the login feature in Sprint 3 (FOOD-6)
     */
    public boolean verifyCredentials(String email, String plainPassword) {
        User user = getUserByEmail(email);
        if (user == null) {
            return false;
        }
        return PasswordUtils.verifyPassword(plainPassword, user.getSalt(), user.getPasswordHash());
    }

    /**
     * Updates a user's password. Generates a new salt and re-hashes.
     * Used by the password reset feature (FOOD-7).
     *
     * @param userId          the ID of the user whose password to update
     * @param newPlainPassword the new plaintext password (will be hashed)
     * @return the number of rows affected
     */
    public int updatePassword(long userId, String newPlainPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(newPlainPassword, salt);

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        values.put(UserEntry.COLUMN_SALT, salt);

        String selection = UserEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        return db.update(UserEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Helper method to convert a database cursor row into a User object.
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UserEntry._ID)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_PASSWORD_HASH)));
        user.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_SALT)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_ROLE)));

        int restaurantIdIndex = cursor.getColumnIndexOrThrow(UserEntry.COLUMN_RESTAURANT_ID);
        if (!cursor.isNull(restaurantIdIndex)) {
            user.setRestaurantId(cursor.getLong(restaurantIdIndex));
        } else {
            user.setRestaurantId(-1);
        }

        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_CREATED_AT)));
        return user;
    }
}
