package com.rettiwer.pl.laris.data.local;

import android.content.SharedPreferences;

public abstract class LocalDatabaseManager {
    private SharedPreferences sharedPreferences;

    public LocalDatabaseManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return this.sharedPreferences.getString(key, null);
    }
}
