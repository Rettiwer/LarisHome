package com.rettiwer.pl.laris.ui.server;

import android.os.Bundle;
import android.text.TextUtils;

import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.server.ServerConfiguration;
import com.rettiwer.pl.laris.data.remote.ConnectionEvent;
import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import io.reactivex.disposables.CompositeDisposable;

import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.NETWORK_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.TIMEOUT;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_API_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_ERROR;

public class ServerActivity extends AppCompatActivity {
    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        ServerConfiguration serverConfiguration = new ServerConfiguration(getApplicationContext());

        if (!TextUtils.isEmpty(serverConfiguration.getLocalServerAddress())) {
            ConnectionManager mConnectionManager = App.getInstance(getApplicationContext()).getConnectionManager();

         /*   mConnectionManager.getRetrofit()
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            retrofit -> {
                                TokenApi tokenApi = retrofit.create(TokenApi.class);
                                Token token = new Token();
                                token.setRefreshToken(new AccountConfiguration(getApplicationContext()).getRefreshToken());
                                tokenApi.updateToken(token).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(new SingleCallbackWrapper<ApiResponse<Token>>() {
                                            @Override
                                            protected void onReceive(ApiResponse<Token> tokenApiResponse) {
                                                Token token = tokenApiResponse.getData().get(0);

                                                new AccountConfiguration(getApplicationContext()).saveRefreshToken(token.getRefreshToken());
                                                    App.getInstance(getApplicationContext()).setToken(token);
                                                }
                                        });
                            });*/
            Navigation.findNavController(this, R.id.server_graph_host).navigate(R.id.authenticationActivity);
            finish();
        }
    }

    public CompositeDisposable getCompositeDisposable() {
        return mDisposable;
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    @Subscribe
    public void connectionEvent(ConnectionEvent connectionEvent) {
        if (connectionEvent.getData() == R.string.permissions_not_granted) {
            //PermissionsUtil.askConnectionPermissions(this, mConnectionManager);
        }
    }

    @Subscribe
    public void requestExceptionEventHandler(RequestExceptionEvent event) {
        @RequestExceptionEvent.RequestExceptionType String exception = event.getRequestExceptionType();

        switch (exception) {
            case TIMEOUT: {
              //  Log.d("a", R.string.request_timeout);
                break;
            }
            case NETWORK_ERROR: {
             //   Snackbar.make(mView, R.string.network_api_error, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case UNKNOWN_API_ERROR:

            case UNKNOWN_ERROR: {
               // Snackbar.make(mView, event.getUnknownError(), Snackbar.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
