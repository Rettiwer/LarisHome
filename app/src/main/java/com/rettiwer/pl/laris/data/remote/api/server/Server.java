package com.rettiwer.pl.laris.data.remote.api.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Server {
    @SerializedName("torAddress")
    @Expose
    private String torAddress;

    private String localAddress;

    public String getTorAddress() {
        return torAddress;
    }

    public void setTorAddress(String torAddress) {
        this.torAddress = torAddress;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }
}
