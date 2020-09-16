package com.rettiwer.pl.laris.data.local.server;

import android.content.Context;
import android.text.TextUtils;

import com.rettiwer.pl.laris.data.local.LocalDatabaseManager;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class ServerConfiguration extends LocalDatabaseManager {
    private static final String LOCAL_NETWORK_SSID = "LOCAL_NETWORK_SSID";
    private static final String LOCAL_SERVER_ADDRESS = "LOCAL_SERVER_ADDRESS";
    private static final String TOR_SERVER_ADDRESS = "TOR_SERVER_ADDRESS";

    public ServerConfiguration(Context context) {
        super(getDefaultSharedPreferences(context));
    }

    public boolean isConfigurationEmpty() {
        return TextUtils.isEmpty(getString(LOCAL_NETWORK_SSID)) ||
            TextUtils.isEmpty(getString(LOCAL_SERVER_ADDRESS)) ||
            TextUtils.isEmpty(getString(TOR_SERVER_ADDRESS));
    }

    public String getLocalNetworkSSID() {
        return getString(LOCAL_NETWORK_SSID);
    }

    public void saveLocalNetworkSSID(String localNetworkSSID) {
        saveString(LOCAL_NETWORK_SSID, localNetworkSSID);
    }

    public String getLocalServerAddress() {
        return getString(LOCAL_SERVER_ADDRESS);
    }

    public void saveLocalServerAddress(String localServerAddress) {
        saveString(LOCAL_SERVER_ADDRESS, localServerAddress);
    }

    public String getTorServerAddress() {
        return getString(TOR_SERVER_ADDRESS);
    }

    public void saveTorServerAddress(String torServerAddress) {
        saveString(TOR_SERVER_ADDRESS, torServerAddress);
    }
}
