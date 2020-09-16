package com.rettiwer.pl.laris.ui.server;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.mannodermaus.rxbonjour.DiscoveryFailedException;
import de.mannodermaus.rxbonjour.RxBonjour;
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver;
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;
import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.server.ServerConfiguration;
import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.NetworkStateReceiver;
import com.rettiwer.pl.laris.ui.authentication.AuthenticationActivity;
import com.rettiwer.pl.laris.utils.NetworkUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;


public class SearchServerFragment extends Fragment implements NetworkStateReceiver.NetworkStateReceiverListener {
    @BindView(R.id.connect_to_local_network_text)
    TextView mSearchingServerText;

    private View mView;

    private ServerActivity mServerActivity;

    private String mServerAddress;

    private RxBonjour mRxBonjour;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_search_server, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, mView);

        App.getInstance(mServerActivity.getApplicationContext()).getConnectionManager()
                .getNetworkStateReceiver().addListener(this);
    }

    private void findServer() {
        mRxBonjour = new RxBonjour.Builder()
                .platform(AndroidPlatform.create(mServerActivity.getApplicationContext()))
                .driver(JmDNSDriver.create())
                .create();

        mServerActivity.getCompositeDisposable().add(mRxBonjour.newDiscovery("_http._tcp")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        event -> {
                            if (event.getService().getName().equalsIgnoreCase("laris-server")) {
                                mSearchingServerText.setText(getText(R.string.found_server));

                                mServerAddress = event.getService().getHost().getHostAddress();

                                getWiifSSID();
                            }
                        },
                        error -> {
                            mSearchingServerText.setText(getString(R.string.error_occurred, error.getMessage()));
                        }
                ));
    }

    private void getWiifSSID() {
        mServerActivity.getCompositeDisposable().add(new RxPermissions(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION).subscribe(granted -> {
                    if (granted) {
                        ServerConfiguration serverConfiguration = new ServerConfiguration(mServerActivity.getApplicationContext());
                        serverConfiguration.saveLocalNetworkSSID(NetworkUtils.getWifiSSID(mServerActivity.getApplicationContext()));
                        serverConfiguration.saveLocalServerAddress(mServerAddress);

                        final Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            Navigation.findNavController(mView).navigate(R.id.authenticationActivity);
                            mServerActivity.finish();
                        }, 1000);
                    }
                    else {
                        getWiifSSID();
                        mSearchingServerText.setText(getText(R.string.permissions_not_granted));
                    }
        }));
    }

    @Override
    public void networkAvailable(NetworkStateReceiver.ConnectionType connectionType) {
        mSearchingServerText.setText(getText(R.string.searching_server));
        findServer();
    }

    @Override
    public void networkUnavailable() {
        mSearchingServerText.setText(getText(R.string.connect_to_local_network));
        mServerActivity.getCompositeDisposable().clear();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mServerActivity = ((ServerActivity) context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        App.getInstance(mServerActivity.getApplicationContext()).getConnectionManager()
                .getNetworkStateReceiver().removeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mServerActivity.getCompositeDisposable().clear();
    }
}
