package com.rettiwer.pl.laris.data.local.user;

import android.content.Context;

import com.rettiwer.pl.laris.data.local.LocalDatabaseManager;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AccountConfiguration extends LocalDatabaseManager {
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN";

    public AccountConfiguration(Context context) {
        super(getDefaultSharedPreferences(context));
    }

    public void saveRefreshToken(String refreshToken) {
        saveString(REFRESH_TOKEN_KEY, refreshToken);
    }

    public String getRefreshToken() {
        return getString(REFRESH_TOKEN_KEY);
    }
}
