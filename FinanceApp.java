package com.finance.manager;

import android.app.Application;

public class FinanceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme when app starts
        PreferenceManager preferenceManager = PreferenceManager.getInstance(this);
        preferenceManager.getTheme();
    }
}