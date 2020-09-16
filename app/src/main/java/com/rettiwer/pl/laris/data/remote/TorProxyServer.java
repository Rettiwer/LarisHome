package com.rettiwer.pl.laris.data.remote;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TorProxyServer implements Runnable {
    private static final String TAG = TorProxyServer.class.getName();

    private Thread mThread;

    private Socket mProxySocket;
    private int mRemotePort;
    private int mLocalPort;

    private boolean mIsRunning = true;

    public TorProxyServer(Socket proxyocket, int remoteport, int localport) {
        this.mProxySocket = proxyocket;
        this.mRemotePort = remoteport;
        this.mLocalPort = localport;

        Log.d(TAG, mLocalPort + "");

        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        Log.d(TAG, mLocalPort + "");
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(mLocalPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final byte[] request = new byte[1024];
        byte[] reply = new byte[4096];

        while (mIsRunning) {
            Socket client = null, server = null;
            try {
                client = ss.accept();
                final InputStream streamFromClient = client.getInputStream();
                final OutputStream streamToClient = client.getOutputStream();

                server = mProxySocket;

                final InputStream streamFromServer = server.getInputStream();
                final OutputStream streamToServer = server.getOutputStream();

                Thread mServerReadThread = new Thread() {
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
                        }
                    }
                };
                mServerReadThread.start();

                int bytesRead;
                try {
                    while ((bytesRead = streamFromServer.read(reply)) != -1) {
                        streamToClient.write(reply, 0, bytesRead);
                        streamToClient.flush();
                    }
                } catch (IOException e) {
                }

                streamToClient.close();
            } catch (IOException e) {
            } finally {
                try {
                    if (server != null)
                        server.close();
                    if (client != null)
                        client.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void stop() {
        mIsRunning = false;
    }
}
