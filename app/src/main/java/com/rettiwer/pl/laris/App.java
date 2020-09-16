package com.rettiwer.pl.laris;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.StrictMode;
import android.util.Log;


import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.api.ApiController;
import com.rettiwer.pl.laris.data.remote.api.token.Token;

import java.net.InetAddress;
import java.net.UnknownHostException;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {
    private ConnectionManager mConnectionManager;

    private Token token;

    @Override
    public void onCreate() {
        super.onCreate();

        mConnectionManager = new ConnectionManager(getApplicationContext());
        mConnectionManager.registerNetworkStateReciver();
    }

    public ConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public Token getToken() {
        return this.token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public static App getInstance(Context context) {
        return ((App) context.getApplicationContext());
    }
}
