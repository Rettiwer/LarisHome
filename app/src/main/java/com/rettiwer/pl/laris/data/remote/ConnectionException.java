package com.rettiwer.pl.laris.data.remote;

public class ConnectionException extends Exception {
    int mMessageId;

    public ConnectionException(int messageId) {
        this.mMessageId = messageId;
    }

    public int getMessageId() {
        return mMessageId;
    }
}
