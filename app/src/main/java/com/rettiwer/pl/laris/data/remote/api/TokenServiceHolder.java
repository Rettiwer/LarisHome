package com.rettiwer.pl.laris.data.remote.api;

import android.content.Context;

import com.rettiwer.pl.laris.data.remote.api.token.TokenService;

import androidx.annotation.Nullable;

public class TokenServiceHolder {
    private TokenService tokenService = null;
    private Context mContext;

    public TokenServiceHolder(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Nullable
    public TokenService get() {
        return tokenService;
    }

    public void set(TokenService tokenService) {
        this.tokenService = tokenService;
    }
}
