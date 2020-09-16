package com.rettiwer.pl.laris.data.remote.tor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.sf.msopentech.thali.java.toronionproxy.OnionProxyManager;
import net.sf.msopentech.thali.java.toronionproxy.android.AndroidOnionProxyManagerThreaded;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class TorTest {

    public static class TorStart extends AsyncTask<String, String, String> {
        private Context context;
        private boolean mIsConnected;

        public TorStart(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... urls) {

            AndroidOnionProxyManagerThreaded onionProxyManager =
                    new AndroidOnionProxyManagerThreaded(context, "torfiles");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                /*boolean ok = onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");
                    */
                while (!onionProxyManager.getOnionProxyManagerThreaded().isRunning())
                    Thread.sleep(90);
                Log.i("TorTest", "Tor initialized on port " + onionProxyManager.getOnionProxyManagerThreaded().getIPv4LocalHostSocksPort());



            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "con";
        }

        protected void onPostExecute(String feed) {

        }

        private void runServer(Socket proxyocket, int remoteport, int localport) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(localport);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final byte[] request = new byte[1024];
            byte[] reply = new byte[4096];

            while (true) {
                mIsConnected = true;
                Socket client = null, server = null;
                try {
                    client = ss.accept();
                    final InputStream streamFromClient = client.getInputStream();
                    final OutputStream streamToClient = client.getOutputStream();

                    server = proxyocket;

                    final InputStream streamFromServer = server.getInputStream();
                    final OutputStream streamToServer = server.getOutputStream();

                    Thread t = new Thread() {
                        public void run() {

                            int bytesRead;
                            try {
                                while ((bytesRead = streamFromClient.read(request)) != -1) {
                                    streamToServer.write(request, 0, bytesRead);
                                    streamToServer.flush();
                                }
                            } catch (IOException e) {
                            }

                            try {
                                streamToServer.close();
                            } catch (IOException e) {
                                Log.e("TorTest", e.getMessage());
                            }
                        }
                    };

                    t.start();

                    int bytesRead;
                    try {
                        while ((bytesRead = streamFromServer.read(reply)) != -1) {
                            streamToClient.write(reply, 0, bytesRead);
                            streamToClient.flush();
                        }
                    } catch (IOException e) {
                        Log.e("TorTest", e.getMessage());
                    }

                    streamToClient.close();
                } catch (IOException e) {
                    Log.e("TorTest", e.getMessage());
                } finally {
                    try {
                        if (server != null)
                            server.close();
                        if (client != null)
                            client.close();
                    } catch (IOException e) {
                        Log.e("TorTest", e.getMessage());
                    }
                }
            }
        }

        public boolean isConnected() {
            return mIsConnected;
        }
    }
}
