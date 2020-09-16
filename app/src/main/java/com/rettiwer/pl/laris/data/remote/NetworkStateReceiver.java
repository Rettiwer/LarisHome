package com.rettiwer.pl.laris.data.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.HashSet;
import java.util.Set;

public class NetworkStateReceiver extends BroadcastReceiver {

    protected Set<NetworkStateReceiverListener> listeners;
    protected ConnectionType connectionType;

    public NetworkStateReceiver() {
        listeners = new HashSet<>();
        connectionType = null;
    }

    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                connectionType = ConnectionType.MOBILE;

            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                connectionType = ConnectionType.WIFI;
            }
        }
        else
            connectionType = null;

        notifyStateToAll();
    }

    public void notifyStateToAll() {
        for(NetworkStateReceiverListener listener : listeners)
            notifyState(listener);
    }

    private void notifyState(NetworkStateReceiverListener listener) {
        if(listener == null)
            return;

        if(connectionType == null)
            listener.networkUnavailable();
        else
            listener.networkAvailable(connectionType);
    }

    public void addListener(NetworkStateReceiverListener l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }


    public interface NetworkStateReceiverListener {
        void networkAvailable(ConnectionType connectionType);
        void networkUnavailable();
    }

    public enum ConnectionType {
        WIFI,
        MOBILE
    }
}