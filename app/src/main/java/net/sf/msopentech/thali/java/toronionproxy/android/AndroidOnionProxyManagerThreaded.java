/*
Copyright (C) 2011-2014 Sublime Software Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.

See the Apache 2 License for the specific language governing permissions and limitations under the License.
*/

package net.sf.msopentech.thali.java.toronionproxy.android;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import net.sf.msopentech.thali.java.toronionproxy.OnionProxyManagerThreaded;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY;

public class AndroidOnionProxyManagerThreaded implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidOnionProxyManagerThreaded.class);
    private final Context context;
    private volatile BroadcastReceiver networkStateReceiver;
    private Thread thread;

    private AndroidOnionProxyContext androidOnionProxyContext;
    private OnionProxyManagerThreaded onionProxyManagerThreaded;

    private boolean isRunning;

    public AndroidOnionProxyManagerThreaded(Context context, String workingSubDirectoryName) {
        androidOnionProxyContext = new AndroidOnionProxyContext(context, workingSubDirectoryName);
        onionProxyManagerThreaded = new OnionProxyManagerThreaded(androidOnionProxyContext);
        this.context = context;

        thread = new Thread(this, "AndroidOnionProxyManagerThread");
        thread.start();
    }

    @Override
    public void run() {
        try {
            androidOnionProxyContext.installFiles();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!setExecutable(androidOnionProxyContext.getTorExecutableFile())) {
            throw new RuntimeException("could not make Tor executable.");
        }
        int totalSecondsPerTorStartup = 4 * 60;
        int totalTriesPerTorStartup = 5;

        try {
            onionProxyManagerThreaded.startWithRepeat(totalSecondsPerTorStartup,  totalTriesPerTorStartup );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isRunning = true;

        while (isRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            onionProxyManagerThreaded.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
      /*  networkStateReceiver = new NetworkStateReceiver();
        IntentFilter filter = new IntentFilter(CONNECTIVITY_ACTION);
        context.registerReceiver(networkStateReceiver, filter);
*/
    }

    public OnionProxyManagerThreaded getOnionProxyManagerThreaded() {
        return this.onionProxyManagerThreaded;
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
        }
    }

/*

    @Override
    public boolean installAndStartTorOp() throws IOException, InterruptedException {
        if (super.installAndStartTorOp()) {
            // Register to receive network status events
            networkStateReceiver = new NetworkStateReceiver();
            IntentFilter filter = new IntentFilter(CONNECTIVITY_ACTION);
            context.registerReceiver(networkStateReceiver, filter);
            return true;
        }
        return false;
    }

    @Override
    public void stop() throws IOException {
        try {
            super.stop();
        } finally {
            if (networkStateReceiver != null) {
                try {
                    if (networkStateReceiver != null) {
                        context.unregisterReceiver(networkStateReceiver);
                    }
                } catch(IllegalArgumentException e) {
                    // There is a race condition where if someone calls stop before installAndStartTorOp is done
                    // then we could get an exception because the network state receiver might not be properly
                    // registered.
                    LOG.info("Someone tried to call stop before we had finished registering the receiver", e);
                }
            }
        }
    }*/

    @SuppressLint("NewApi")
    protected boolean setExecutable(File f) {
        if (Build.VERSION.SDK_INT >= 9) {
            return f.setExecutable(true, true);
        } else {
            String[] command = {"chmod", "700", f.getAbsolutePath()};
            try {
                return Runtime.getRuntime().exec(command).waitFor() == 0;
            } catch (IOException e) {
                LOG.warn(e.toString(), e);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while executing chmod");
                Thread.currentThread().interrupt();
            } catch (SecurityException e) {
                LOG.warn(e.toString(), e);
            }
            return false;
        }
    }

    private class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent i) {
            try {
                if (!onionProxyManagerThreaded.isRunning()) return;
            } catch (IOException e) {
                LOG.info("Did someone call before Tor was ready?", e);
                return;
            }
            boolean online = !i.getBooleanExtra(EXTRA_NO_CONNECTIVITY, false);
            if (online) {
                // Some devices fail to set EXTRA_NO_CONNECTIVITY, double check
                Object o = ctx.getSystemService(CONNECTIVITY_SERVICE);
                ConnectivityManager cm = (ConnectivityManager) o;
                NetworkInfo net = cm.getActiveNetworkInfo();
                if (net == null || !net.isConnected()) online = false;
            }
            LOG.info("Online: " + online);
            try {
                onionProxyManagerThreaded.enableNetwork(online);
            } catch (IOException e) {
                LOG.warn(e.toString(), e);
            }
        }
    }
}