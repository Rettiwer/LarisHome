package com.rettiwer.pl.laris.data.remote;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.server.ServerConfiguration;
import com.rettiwer.pl.laris.data.remote.api.ApiController;
import com.rettiwer.pl.laris.data.remote.mqtt.MqttClientService;
import com.rettiwer.pl.laris.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import androidx.core.content.ContextCompat;
import io.reactivex.Single;
import retrofit2.Retrofit;

public class ConnectionManager implements NetworkStateReceiver.NetworkStateReceiverListener {
    private Context mContext;
    private NetworkStateReceiver mNetworkStateReceiver;

    private ApiController mApiController;

    private Retrofit mRetrofit;

    private MqttClientService mMqttClientService;

    private TorConnectionManager mTorConnectionManager;
    private ServerConfiguration mServerConfiguration;

    private ConnectionMethod mConnectionMethod;


    public ConnectionManager(Context context) {
        this.mContext = context;

        this.mServerConfiguration = new ServerConfiguration(mContext);

        this.mTorConnectionManager = new TorConnectionManager(mContext);
    }

    @Override
    public void networkAvailable(NetworkStateReceiver.ConnectionType connectionType) {
        if (connectionType.equals(NetworkStateReceiver.ConnectionType.MOBILE)) {
           runTorConnection();
        }

        if (connectionType.equals(NetworkStateReceiver.ConnectionType.WIFI)) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                if (NetworkUtils.getWifiSSID(mContext).equals(mServerConfiguration.getLocalNetworkSSID())) {
                    runLocalConnection();
                }
                else {
                    if (!TextUtils.isEmpty(mServerConfiguration.getTorServerAddress())) {
                        runTorConnection();
                    }
                    else {
                        mConnectionMethod = null;
                    }
                }
            }
            else {
                mConnectionMethod = ConnectionMethod.PERMISSIONS_NOT_GRANTED;
            }
        }
    }

    private void runLocalConnection() {
        mConnectionMethod = ConnectionMethod.DEFAULT;

        mTorConnectionManager.stop();
        mApiController = new ApiController(mServerConfiguration, mContext);
        mRetrofit = mApiController.init();

        mMqttClientService = new MqttClientService(mContext);
        mMqttClientService.connect(mServerConfiguration.getLocalServerAddress(), "rettiwer", "123456");
    }

    private void runTorConnection() {
        mConnectionMethod = ConnectionMethod.TOR;

        mTorConnectionManager.run();

        new Thread(() -> {
            while (mTorConnectionManager.getConnectionStatus() != TorConnectionManager.TorConnectionStatus.CONNECTED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mApiController = new ApiController(mServerConfiguration, mTorConnectionManager.getPort(), mContext);
            mRetrofit = mApiController.init();
        }).start();
    }


    @Override
    public void networkUnavailable() {
        mRetrofit = null;
        mConnectionMethod = null;
        mMqttClientService.disconnect();
    }

    public void permissionsGranted() {
        if (mNetworkStateReceiver != null) {
            mNetworkStateReceiver.notifyStateToAll();
        }
    }

    public Single<Retrofit> getRetrofit() {
        return Single.create(emitter -> {
            if (mRetrofit != null) {
                emitter.onSuccess(this.mRetrofit);
            }
            if (!emitter.isDisposed()) {
                if (mConnectionMethod == ConnectionMethod.TOR) {
                    switch (mTorConnectionManager.getConnectionStatus()) {
                        case CONNECTING:
                            emitter.onError(new ConnectionException(R.string.connecting_to_tor_network));
                            break;
                        case CANNOT_CONNECT:
                            emitter.onError(new ConnectionException(R.string.cannot_connect_to_tor_network));
                            break;
                    }
                }
                else if(mConnectionMethod == ConnectionMethod.PERMISSIONS_NOT_GRANTED) {
                    EventBus.getDefault().post(new ConnectionEvent(R.string.permissions_not_granted));
                }
                else if (mConnectionMethod == null) {
                    emitter.onError(new ConnectionException(R.string.no_connection));
                }
            }
        });
    }

    public MqttClientService getMqttClientService() {
        if (mMqttClientService != null) {
            return mMqttClientService;
        }
        return null;
    }

    public void registerNetworkStateReciver() {
        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
        mContext.registerReceiver(mNetworkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void unregisterNetworkStateReciver() {
        mNetworkStateReceiver.removeListener(this);
        mContext.unregisterReceiver(mNetworkStateReceiver);
    }

    public NetworkStateReceiver getNetworkStateReceiver() {
        return mNetworkStateReceiver;
    }

    public enum ConnectionMethod {
        DEFAULT,
        TOR,
        PERMISSIONS_NOT_GRANTED
    }
}
