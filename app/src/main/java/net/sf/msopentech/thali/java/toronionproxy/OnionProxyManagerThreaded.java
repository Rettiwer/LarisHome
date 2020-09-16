package net.sf.msopentech.thali.java.toronionproxy;

import net.sf.controller.network.NetLayerStatus;
import net.sf.controller.network.ServiceDescriptor;
import net.sf.freehaven.tor.control.ConfigEntry;
import net.sf.freehaven.tor.control.TorControlConnection;
import net.sf.runjva.sourceforge.jsocks.protocol.Socks5Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class OnionProxyManagerThreaded {

    private static final String[] EVENTS = {
            "CIRC", "ORCONN", "NOTICE", "WARN", "ERR"
    };

    private static final String OWNER = "__OwningControllerProcess";
    private static final int COOKIE_TIMEOUT = 3 * 1000; // Milliseconds
    private static final int HOSTNAME_TIMEOUT = 30 * 1000; // Milliseconds

    private static final int TOTAL_SEC_PER_STARTUP = 4 * 60;
    private static final int TRIES_PER_STARTUP = 5;

    private static final Logger LOG = LoggerFactory.getLogger(OnionProxyManager.class);

    protected final OnionProxyContext onionProxyContext;

    private volatile Socket controlSocket = null;

    // If controlConnection is not null then this means that a connection exists and the Tor OP will die when
    // the connection fails.
    private volatile TorControlConnection controlConnection = null;
    private volatile int control_port;
    private OnionProxyManagerEventHandler eventHandler;

    private Socks5Proxy proxy;
    static final String PROXY_LOCALHOST = "127.0.0.1";

    public OnionProxyContext getOnionProxyContext() {
        return onionProxyContext;
    }


    public OnionProxyManagerThreaded(OnionProxyContext onionProxyContext) {
        this.onionProxyContext = onionProxyContext;
        eventHandler = new OnionProxyManagerEventHandler();
    }


    public synchronized boolean startWithRepeat(int secondsBeforeTimeOut, int numberOfRetries) throws
            InterruptedException, IOException {
        if (secondsBeforeTimeOut <= 0 || numberOfRetries < 0) {
            throw new IllegalArgumentException("secondsBeforeTimeOut >= 0 & numberOfRetries > 0");
        }

        try {
            for (int retryCount = 0; retryCount < numberOfRetries; ++retryCount) {
                if (!installAndStartTorOp()) {
                    return false;
                }
                enableNetwork(true);

                // We will check every second to see if boot strapping has finally finished
                for (int secondsWaited = 0; secondsWaited < secondsBeforeTimeOut; ++secondsWaited) {
                    if (!isBootstrapped()) {
                        Thread.sleep(1000, 0);
                    } else {
                        return true;
                    }
                }

                // Bootstrapping isn't over so we need to restart and try again
                stop();
                // Experimentally we have found that if a Tor OP has run before and thus has cached descriptors
                // and that when we try to start it again it won't start then deleting the cached data can fix this.
                // But, if there is cached data and things do work then the Tor OP will start faster than it would
                // if we delete everything.
                // So our compromise is that we try to start the Tor OP 'as is' on the first round and after that
                // we delete all the files.
                onionProxyContext.deleteAllFilesButHiddenServices();
            }

            return false;
        } finally {
            // Make sure we return the Tor OP in some kind of consistent state, even if it's 'off'.
            if (!isRunning()) {
                stop();
            }
        }
    }


    public synchronized int getIPv4LocalHostSocksPort() throws IOException {
        if (!isRunning()) {
            throw new RuntimeException("Tor is not running!");
        }

        // This returns a set of space delimited quoted strings which could be Ipv4, Ipv6 or unix sockets
        String[] socksIpPorts = controlConnection.getInfo("net/listeners/socks").split(" ");

        for (String address : socksIpPorts) {
            if (address.contains("\"127.0.0.1:")) {
                // Remember, the last character will be a " so we have to remove that
                return Integer.parseInt(address.substring(address.lastIndexOf(":") + 1, address.length() - 1));
            }
        }

        throw new RuntimeException("We don't have an Ipv4 localhost binding for socks!");
    }

    public Socks5Proxy SetupSocks5Proxy(int proxyPort) throws UnknownHostException {
        Socks5Proxy proxy = new Socks5Proxy(PROXY_LOCALHOST, proxyPort);
        proxy.resolveAddrLocally(false);
        return proxy;
    }

    public synchronized void stop() throws IOException {
        try {
            if (controlConnection == null) {
                return;
            }
            LOG.info("Stopping Tor");
            controlConnection.setConf("DisableNetwork", "1");
            controlConnection.shutdownTor("TERM");
        } finally {
            if (controlSocket != null) {
                controlSocket.close();
            }
            controlConnection = null;
            controlSocket = null;
        }
    }


    public synchronized boolean isRunning() throws IOException {
        return isBootstrapped() && isNetworkEnabled();
    }


    public synchronized void enableNetwork(boolean enable) throws IOException {
        if (controlConnection == null) {
            throw new RuntimeException("Tor is not running!");
        }
        LOG.info("Enabling network: " + enable);
        controlConnection.setConf("DisableNetwork", enable ? "0" : "1");
    }


    public synchronized boolean isNetworkEnabled() throws IOException {
        if (controlConnection == null) {
            throw new RuntimeException("Tor is not running!");
        }

        List<ConfigEntry> disableNetworkSettingValues = controlConnection.getConf("DisableNetwork");
        boolean result = false;
        // It's theoretically possible for us to get multiple values back, if even one is false then we will
        // assume all are false
        for (ConfigEntry configEntry : disableNetworkSettingValues) {
            if (configEntry.value.equals("1")) {
                return false;
            } else {
                result = true;
            }
        }
        return result;
    }


    public synchronized boolean isBootstrapped() {
        if (controlConnection == null) {
            return false;
        }

        String phase = null;
        try {
            phase = controlConnection.getInfo("status/bootstrap-phase");
        } catch (IOException e) {
            LOG.warn("Control connection is not responding properly to getInfo", e);
        }

        if (phase != null && phase.contains("PROGRESS=100")) {
            LOG.info("Tor has already bootstrapped");
            return true;
        }

        return false;
    }


    public synchronized boolean installAndStartTorOp() throws IOException, InterruptedException {
        // The Tor OP will die if it looses the connection to its socket so if there is no controlSocket defined
        // then Tor is dead. This assumes, of course, that takeOwnership works and we can't end up with Zombies.
        if (controlConnection != null) {
            LOG.info("Tor is already running");
            return true;
        }

        // The code below is why this method is synchronized, we don't want two instances of it running at once
        // as the result would be a mess of screwed up files and connections.
        LOG.info("Tor is not running");

        installAndConfigureFiles();

        LOG.info("Starting Tor");
        File cookieFile = onionProxyContext.getCookieFile();
        if (!cookieFile.getParentFile().exists()
                && !cookieFile.getParentFile().mkdirs()) {
            throw new RuntimeException("Could not create cookieFile parent directory");
        }

        // The original code from Briar watches individual files, not a directory and Android's file observer
        // won't work on files that don't exist. Rather than take 5 seconds to rewrite Briar's code I instead
        // just make sure the file exists
        if (!cookieFile.exists() && !cookieFile.createNewFile()) {
            throw new RuntimeException("Could not create cookieFile");
        }

        File workingDirectory = onionProxyContext.getWorkingDirectory();
        // Watch for the auth cookie file being created/updated
        WriteObserver cookieObserver = onionProxyContext.generateWriteObserver(cookieFile);
        // Start a new Tor process
        String torPath = onionProxyContext.getTorExecutableFile().getAbsolutePath();
        String configDir = onionProxyContext.getTorrcFile().getParent();
        String configPath = onionProxyContext.getTorrcFile().getAbsolutePath();
        String pid = onionProxyContext.getProcessId();
        String[] cmd = {torPath, "-f", configPath, OWNER, pid};
        String[] env = onionProxyContext.getEnvironmentArgsForExec();
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.directory(new File(configDir));
        processBuilder.redirectErrorStream(true);
        onionProxyContext.setEnvironmentArgsAndWorkingDirectoryForStart(processBuilder);
        Process torProcess = null;
        try {
//            torProcess = Runtime.getRuntime().exec(cmd, env, workingDirectory);
            torProcess = processBuilder.start();
            CountDownLatch controlPortCountDownLatch = new CountDownLatch(1);
            eatStream(torProcess.getInputStream(), false, controlPortCountDownLatch);
            eatStream(torProcess.getErrorStream(), true, null);

            // On platforms other than Windows we run as a daemon and so we need to wait for the process to detach
            // or exit. In the case of Windows the equivalent is running as a service and unfortunately that requires
            // managing the service, such as turning it off or uninstalling it when it's time to move on. Any number
            // of errors can prevent us from doing the cleanup and so we would leave the process running around. Rather
            // than do that on Windows we just let the process run on the exec and hence don't look for an exit code.
            // This does create a condition where the process has exited due to a problem but we should hopefully
            // detect that when we try to use the control connection.
            if (OsData.getOsType() != OsData.OsType.WINDOWS) {
                int exit = torProcess.waitFor();
                torProcess = null;
                if (exit != 0) {
                    LOG.warn("Tor exited with value " + exit);
                    return false;
                }
            }

            // Wait for the auth cookie file to be created/updated
            if (!cookieObserver.poll(COOKIE_TIMEOUT, MILLISECONDS)) {
                LOG.warn("Auth cookie not created");
                FileUtilities.listFilesToLog(workingDirectory);
                return false;
            }

            // Now we should be able to connect to the new process
            controlPortCountDownLatch.await();
            controlSocket = new Socket("127.0.0.1", control_port);

            // Open a control connection and authenticate using the cookie file
            TorControlConnection controlConnection = new TorControlConnection(controlSocket);
            controlConnection.authenticate(FileUtilities.read(cookieFile));
            // Tell Tor to exit when the control connection is closed
            controlConnection.takeOwnership();
            controlConnection.resetConf(Collections.singletonList(OWNER));
            // Register to receive events from the Tor process
            controlConnection.setEventHandler(new OnionProxyManagerEventHandler());
            controlConnection.setEvents(Arrays.asList(EVENTS));
            // We only set the class property once the connection is in a known good state
            this.controlConnection = controlConnection;
            return true;
        } catch (SecurityException e) {
            LOG.warn(e.toString(), e);
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while starting Tor", e);
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (controlConnection == null && torProcess != null) {
                // It's possible that something 'bad' could happen after we executed exec but before we takeOwnership()
                // in which case the Tor OP will hang out as a zombie until this process is killed. This is problematic
                // when we want to do things like
                torProcess.destroy();
            }
        }
    }


    public File getWorkingDirectory() {
        return onionProxyContext.getWorkingDirectory();
    }

    protected void eatStream(final InputStream inputStream, final boolean stdError, final CountDownLatch countDownLatch) {
        new Thread() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(inputStream);
                try {
                    while (scanner.hasNextLine()) {
                        if (stdError) {
                            LOG.error(scanner.nextLine());
                        } else {
                            String nextLine = scanner.nextLine();
                            // We need to find the line where it tells us what the control port is.
                            // The line that will appear in stdio with the control port looks like:
                            // Control listener listening on port 39717.
                            if (nextLine.contains("Control listener listening on port ")) {
                                // For the record, I hate regex so I'm doing this manually
                                control_port
                                        = Integer.parseInt(
                                        nextLine.substring(nextLine.lastIndexOf(" ") + 1, nextLine.length() - 1));
                                countDownLatch.countDown();
                            }
                            LOG.info(nextLine);
                        }
                    }
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LOG.error("Couldn't close input stream in eatStream", e);
                    }
                }
            }
        }.start();
    }

    protected synchronized void installAndConfigureFiles() throws IOException, InterruptedException {
        // We need to edit the config file to specify exactly where the cookie/geoip files should be stored, on
        // Android this is always a fixed location relative to the configFiles which is why this extra step
        // wasn't needed in Briar's Android code. But in Windows it ends up in the user's AppData/Roaming. Rather
        // than track it down we just tell Tor where to put it.
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(onionProxyContext.getTorrcFile(), true)));
            printWriter.println("CookieAuthFile " + onionProxyContext.getCookieFile().getAbsolutePath());
            // For some reason the GeoIP's location can only be given as a file name, not a path and it has
            // to be in the data directory so we need to set both
            printWriter.println("DataDirectory " + onionProxyContext.getWorkingDirectory().getAbsolutePath());
            printWriter.println("GeoIPFile " + onionProxyContext.getGeoIpFile().getName());
            printWriter.println("GeoIPv6File " + onionProxyContext.getGeoIpv6File().getName());
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }


    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}