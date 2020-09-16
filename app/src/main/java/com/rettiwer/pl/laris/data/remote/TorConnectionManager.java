package com.rettiwer.pl.laris.data.remote;

import android.content.Context;
import android.util.Log;

import net.sf.msopentech.thali.java.toronionproxy.android.AndroidOnionProxyManagerThreaded;
import net.sf.runjva.sourceforge.jsocks.protocol.Socks5Proxy;

import java.io.IOException;
import java.net.Socket;

public class TorConnectionManager {
    private static final String TAG = TorConnectionManager.class.getName();

    private Context mContext;
    private TorConnectionStatus mTorConnectionStatus = TorConnectionStatus.NOT_CONNECTED;

    private TorConnector mTorConnector;

    private int mPort;

    public TorConnectionManager(Context context) {
        this.mContext = context;
    }

    public void run() {
        mTorConnector = new TorConnector();
    }

    public void stop() {
        if (mTorConnector != null) {
            mTorConnector.stopTor();
        }
    }

    public TorConnectionStatus getConnectionStatus() {
        return mTorConnectionStatus;
    }

    public int getPort()  {
        return this.mPort;
    }

    private class TorConnector implements Runnable {
        private AndroidOnionProxyManagerThreaded  mOnionProxyManager;

        private Socket mMqttSocket;
        private TorProxyServer mMqttProxy;

        public TorConnector() {
            Thread mTorThread = new Thread(this, "TorClientThread");
            mTorThread.start();
        }

        @Override
        public void run() {
            mOnionProxyManager =
                    new AndroidOnionProxyManagerThreaded(mContext, "torfiles");
            try {
                mTorConnectionStatus = TorConnectionStatus.CONNECTING;

               /* boolean ok = mOnionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
                if (!ok)
                    mTorConnectionStatus = TorConnectionStatus.CANNOT_CONNECT;
*/
                while (!mOnionProxyManager.getOnionProxyManagerThreaded().isRunning())
                    Thread.sleep(90);

                Log.i(TAG, "Tor initialized on port " + mOnionProxyManager.getOnionProxyManagerThreaded().getIPv4LocalHostSocksPort());
                mPort = mOnionProxyManager.getOnionProxyManagerThreaded().getIPv4LocalHostSocksPort();

                Socks5Proxy socks5Proxy = new Socks5Proxy("127.0.0.1", mOnionProxyManager.getOnionProxyManagerThreaded().getIPv4LocalHostSocksPort());
                socks5Proxy.resolveAddrLocally(false);

                /* mMqttSocket = Utilities.Socks5connection(socks5Proxy, "augktrgkvx2esc7h.onion", 1883);

               while (mMqttSocket  == null)
                    Thread.sleep(90);
                
                mMqttProxy = new TorProxyServer(mMqttSocket, 1883, 2138);
*/
                mTorConnectionStatus = TorConnectionStatus.CONNECTED;
            } catch (InterruptedException | IOException e) {
                mTorConnectionStatus = TorConnectionStatus.CANNOT_CONNECT;
                Log.e(TAG, e.getMessage());
            }
        }

        public void stopTor()  {
            if (mMqttProxy != null) {
                mMqttProxy.stop();
            }

            if (mOnionProxyManager != null) {
                mOnionProxyManager.stop();
            }
            mTorConnectionStatus = TorConnectionStatus.NOT_CONNECTED;
        }

        public AndroidOnionProxyManagerThreaded getOnionProxyManager() {
            return mOnionProxyManager;
        }
    }

    public enum TorConnectionStatus {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        CANNOT_CONNECT,
    }
}
