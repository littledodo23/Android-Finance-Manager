package com.finance.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PreferenceManager - Singleton pattern for managing SharedPreferences
 * Thread-safe implementation with proper synchronization
 * Handles all app preferences in a centralized manner
 */
public class PreferenceManager {

    // Volatile ensures visibility of changes across threads
    private static volatile PreferenceManager instance;
    private final SharedPreferences sharedPreferences;

    // SharedPreferences file name
    private static final String PREFS_NAME = "FinanceManagerPrefs";

    // Preference Keys - Use constants to avoid typos
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_THEME = "theme";
    private static final String KEY_DEFAULT_PERIOD = "defaultPeriod";
    private static final String KEY_FIRST_LAUNCH = "firstLaunch";
    private static final String KEY_LAST_BACKUP = "lastBackup";
    private static final String KEY_NOTIFICATION_ENABLED = "notificationEnabled";
    private static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";

    // Default values
    private static final String DEFAULT_THEME = "light";
    private static final int DEFAULT_PERIOD = 0;
    private static final boolean DEFAULT_REMEMBER = false;
    private static final boolean DEFAULT_FIRST_LAUNCH = true;
    private static final boolean DEFAULT_NOTIFICATION_ENABLED = true;
    private static final boolean DEFAULT_BIOMETRIC_ENABLED = false;

    /**
     * Private constructor to prevent direct instantiation
     * Always use getInstance() method
     *
     * @param context Application context
     */
    private PreferenceManager(Context context) {
        // Use application context to prevent memory leaks
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Double-checked locking singleton pattern
     * Thread-safe and efficient
     *
     * @param context Application context
     * @return Singleton instance of PreferenceManager
     */
    public static PreferenceManager getInstance(Context context) {
        if (instance == null) { // First check (no locking)
            synchronized (PreferenceManager.class) {
                if (instance == null) { // Second check (with locking)
                    instance = new PreferenceManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * Prevent cloning of singleton instance
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone singleton instance");
    }

    // ==================== REMEMBER ME PREFERENCES ====================

    /**
     * Save remember me preference
     * Thread-safe operation using apply()
     *
     * @param email User email
     * @param remember Whether to remember the user
     */
    public void saveRememberMe(String email, boolean remember) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (remember) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_EMAIL, email);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_EMAIL);
        }

        // Use apply() instead of commit() for async operation
        editor.apply();
    }

    /**
     * Check if remember me is enabled
     *
     * @return true if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER, DEFAULT_REMEMBER);
    }

    /**
     * Get remembered email
     *
     * @return Remembered email or empty string
     */
    public String getRememberedEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    /**
     * Clear remember me data
     */
    public void clearRememberMe() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_REMEMBER);
        editor.remove(KEY_EMAIL);
        editor.apply();
    }

    // ==================== THEME PREFERENCES ====================

    /**
     * Save theme preference
     *
     * @param theme Theme name ("light", "dark", "system")
     */
    public void saveTheme(String theme) {
        sharedPreferences.edit()
                .putString(KEY_THEME, theme)
                .apply();
    }

    /**
     * Get saved theme
     *
     * @return Theme name or default theme
     */
    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, DEFAULT_THEME);
    }

    /**
     * Check if dark mode is enabled
     *
     * @return true if dark mode is enabled
     */
    public boolean isDarkModeEnabled() {
        String theme = getTheme();
        return "dark".equals(theme);
    }

    // ==================== DEFAULT PERIOD PREFERENCES ====================

    /**
     * Save default period selection
     *
     * @param period Period index (0-4)
     */
    public void saveDefaultPeriod(int period) {
        sharedPreferences.edit()
                .putInt(KEY_DEFAULT_PERIOD, period)
                .apply();
    }

    /**
     * Get default period
     *
     * @return Period index
     */
    public int getDefaultPeriod() {
        return sharedPreferences.getInt(KEY_DEFAULT_PERIOD, DEFAULT_PERIOD);
    }

    // ==================== FIRST LAUNCH PREFERENCES ====================

    /**
     * Check if this is first app launch
     *
     * @return true if first launch
     */
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, DEFAULT_FIRST_LAUNCH);
    }

    /**
     * Mark first launch as complete
     */
    public void setFirstLaunchComplete() {
        sharedPreferences.edit()
                .putBoolean(KEY_FIRST_LAUNCH, false)
                .apply();
    }

    // ==================== BACKUP PREFERENCES ====================

    /**
     * Save last backup timestamp
     *
     * @param timestamp Backup timestamp in milliseconds
     */
    public void saveLastBackupTime(long timestamp) {
        sharedPreferences.edit()
                .putLong(KEY_LAST_BACKUP, timestamp)
                .apply();
    }

    /**
     * Get last backup timestamp
     *
     * @return Backup timestamp or 0 if never backed up
     */
    public long getLastBackupTime() {
        return sharedPreferences.getLong(KEY_LAST_BACKUP, 0);
    }

    // ==================== NOTIFICATION PREFERENCES ====================

    /**
     * Set notification enabled state
     *
     * @param enabled true to enable notifications
     */
    public void setNotificationEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
                .apply();
    }

    /**
     * Check if notifications are enabled
     *
     * @return true if notifications are enabled
     */
    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, DEFAULT_NOTIFICATION_ENABLED);
    }

    // ==================== BIOMETRIC PREFERENCES ====================

    /**
     * Set biometric authentication enabled state
     *
     * @param enabled true to enable biometric authentication
     */
    public void setBiometricEnabled(boolean enabled) {
        sharedPreferences.edit()
                .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
                .apply();
    }

    /**
     * Check if biometric authentication is enabled
     *
     * @return true if biometric is enabled
     */
    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, DEFAULT_BIOMETRIC_ENABLED);
    }

    // ==================== GENERIC PREFERENCE METHODS ====================

    /**
     * Save string preference
     *
     * @param key Preference key
     * @param value String value
     */
    public void putString(String key, String value) {
        sharedPreferences.edit()
                .putString(key, value)
                .apply();
    }

    /**
     * Get string preference
     *
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return Stored string value or default
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Save integer preference
     *
     * @param key Preference key
     * @param value Integer value
     */
    public void putInt(String key, int value) {
        sharedPreferences.edit()
                .putInt(key, value)
                .apply();
    }

    /**
     * Get integer preference
     *
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return Stored integer value or default
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Save boolean preference
     *
     * @param key Preference key
     * @param value Boolean value
     */
    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit()
                .putBoolean(key, value)
                .apply();
    }

    /**
     * Get boolean preference
     *
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return Stored boolean value or default
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Save long preference
     *
     * @param key Preference key
     * @param value Long value
     */
    public void putLong(String key, long value) {
        sharedPreferences.edit()
                .putLong(key, value)
                .apply();
    }

    /**
     * Get long preference
     *
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return Stored long value or default
     */
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    /**
     * Remove specific preference
     *
     * @param key Preference key to remove
     */
    public void remove(String key) {
        sharedPreferences.edit()
                .remove(key)
                .apply();
    }

    /**
     * Check if preference exists
     *
     * @param key Preference key
     * @return true if preference exists
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    // ==================== CLEAR ALL PREFERENCES ====================

    /**
     * Clear all preferences
     * Use with caution - this removes ALL stored preferences
     */
    public void clearAllPreferences() {
        sharedPreferences.edit()
                .clear()
                .apply();
    }

    /**
     * Clear all user-specific data while keeping app settings
     * Useful during logout
     */
    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_REMEMBER);
        editor.remove(KEY_LAST_BACKUP);
        // Keep theme and other app settings
        editor.apply();
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Register preference change listener
     *
     * @param listener Listener to register
     */
    public void registerOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregister preference change listener
     *
     * @param listener Listener to unregister
     */
    public void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Get all preferences (for debugging/backup purposes)
     *
     * @return Map of all preferences
     */
    public java.util.Map<String, ?> getAllPreferences() {
        return sharedPreferences.getAll();
    }
}