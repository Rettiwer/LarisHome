package com.rettiwer.pl.laris.data.remote.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.net.Inet4Address;
import java.util.UUID;

public class MqttClientService {
    private static final String TAG = MqttClientService.class.getSimpleName();

    private Context context;

    private MqttAndroidClient mqttAndroidClient;

    public MqttClientService(Context context) {
        this.context = context;
    }


    public boolean connect(String ipAddress, String clientId, String password) {
        String serverURI = "tcp://"+ipAddress+":1883";

        MqttConnectOptions options;
        this.mqttAndroidClient = new MqttAndroidClient(this.context, serverURI, UUID.randomUUID().toString());
        options = new MqttConnectOptions();
        options.setKeepAliveInterval(240);
        options.setAutomaticReconnect(true);
        options.setUserName(clientId);
        options.setPassword(password.toCharArray());



        this.mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                EventBus.getDefault().post(new MqttMessageEvent(topic, message.toString()));
                Log.d("mqtt", message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        final boolean[] success = {false};

        try {
            this.mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    success[0] = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("mqtt", exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return success[0];
    }

    public boolean disconnect() {
        if (this.mqttAndroidClient != null) {
            try {
                this.mqttAndroidClient.disconnect();
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void destroy() {
    }

    public boolean subscribeTopic(String topic, int qos) {
        final boolean[] success = new boolean[1];
        try {
            IMqttToken subToken = this.mqttAndroidClient.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    success[0] = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    success[0] = false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return success[0];
    }

    public MqttAndroidClient getClient() {
        return this.mqttAndroidClient;
    }
}
