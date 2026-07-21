package com.example.foodpickupapp.util;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class for securely hashing and verifying passwords.
 * Uses PBKDF2WithHmacSHA1 with a random salt per user.
 *
 * This approach stores passwords safely in the local SQLite database
 * by never storing the plaintext password — only the hash and salt.
 *
 * Related to: FOOD-9 (store user credentials safely)
 */
public final class PasswordUtils {

    private static final int SALT_LENGTH_BYTES = 16;
    private static final int HASH_ITERATIONS = 10000;
    private static final int HASH_KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    // Private constructor — this is a utility class
    private PasswordUtils() {}

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return Base64-encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Hashes a password using PBKDF2 with the given salt.
     *
     * @param password  the plaintext password
     * @param salt      Base64-encoded salt string
     * @return Base64-encoded hash string
     */
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.decode(salt, Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    HASH_ITERATIONS,
                    HASH_KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored hash and salt.
     *
     * @param password   the plaintext password to verify
     * @param salt       Base64-encoded salt that was used when the password was first hashed
     * @param storedHash Base64-encoded hash to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(storedHash);
    }
}
