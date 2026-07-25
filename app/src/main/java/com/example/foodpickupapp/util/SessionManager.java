package com.example.foodpickupapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple session manager using SharedPreferences to track logged-in user state.
 * Related to: FOOD-6 (login session persistence)
 */
public class SessionManager {

    private static final String PREF_NAME = "FoodPickupSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves a login session after successful authentication.
     *
     * @param userId the logged-in user's ID
     * @param email  the logged-in user's email
     * @param role   the logged-in user's role (STUDENT, STAFF, or ADMIN)
     */
    public void saveSession(long userId, String email, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    /** Returns true if a user is currently logged in. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /** Returns the logged-in user's ID, or -1 if not logged in. */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /** Returns the logged-in user's email, or empty string if not logged in. */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /** Returns the logged-in user's role, or empty string if not logged in. */
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    /**
     * Clears the session (logout).
     */
    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
