package com.finance.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static PreferenceManager instance;
    private SharedPreferences sharedPreferences;
    
    // Keys for preferences
    private static final String PREFS_NAME = "FinanceManagerPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_THEME = "theme";
    private static final String KEY_DEFAULT_PERIOD = "defaultPeriod";
    
    // Private constructor to prevent direct instantiation
    private PreferenceManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Public method to get singleton instance
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }
    
    // ==================== REMEMBER ME PREFERENCES ====================
    
    public void saveRememberMe(String email, boolean remember) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (remember) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_EMAIL, email);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_EMAIL);
        }
        editor.apply();
    }
    
    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER, false);
    }
    
    public String getRememberedEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }
    
    // ==================== THEME PREFERENCES ====================
    
    public void saveTheme(String theme) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply();
    }
    
    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, "light");
    }
    
    // ==================== DEFAULT PERIOD PREFERENCES ====================
    
    public void saveDefaultPeriod(int period) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_PERIOD, period).apply();
    }
    
    public int getDefaultPeriod() {
        return sharedPreferences.getInt(KEY_DEFAULT_PERIOD, 0);
    }
    
    // ==================== CLEAR ALL PREFERENCES ====================
    
    public void clearAllPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}
