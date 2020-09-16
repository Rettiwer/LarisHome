package com.rettiwer.pl.laris.data.remote.mqtt;

public class MqttMessageEvent {
    private String mTopic;
    private String mMessage;

    public MqttMessageEvent(String topic, String message) {
        this.mTopic = topic;
        this.mMessage = message;
    }

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String mTopic) {
        this.mTopic = mTopic;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }
}
