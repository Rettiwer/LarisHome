package com.rettiwer.pl.laris.ui.authentication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.server.ServerConfiguration;
import com.rettiwer.pl.laris.data.local.user.AccountConfiguration;
import com.rettiwer.pl.laris.data.remote.ConnectionEvent;
import com.rettiwer.pl.laris.data.remote.ConnectionException;
import com.rettiwer.pl.laris.data.remote.ConnectionManager;
import com.rettiwer.pl.laris.data.remote.api.ApiResponse;
import com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent;
import com.rettiwer.pl.laris.data.remote.api.SingleCallbackWrapper;
import com.rettiwer.pl.laris.data.remote.api.server.Server;
import com.rettiwer.pl.laris.data.remote.api.server.ServerService;
import com.rettiwer.pl.laris.data.remote.api.token.Token;
import com.rettiwer.pl.laris.data.remote.api.token.TokenService;
import com.rettiwer.pl.laris.utils.PermissionsUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.apache.commons.validator.routines.EmailValidator;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.NETWORK_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.TIMEOUT;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_API_ERROR;
import static com.rettiwer.pl.laris.data.remote.api.RequestExceptionEvent.UNKNOWN_ERROR;

public class LoginFragment extends Fragment {
    @BindView(R.id.password_input_layout)
    TextInputLayout password;

    @BindView(R.id.email_input_layout)
    TextInputLayout email;

    @BindView(R.id.login_button)
    Button login;

    private AuthenticationActivity mAuthenticationActivity;

    private ServerConfiguration mServerController;

    private View mView;

    private ConnectionManager mConnectionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login, container, false);
        return mView;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConnectionManager = App.getInstance(mAuthenticationActivity.getApplicationContext()).getConnectionManager();

        ButterKnife.bind(this, view);

        mServerController = new ServerConfiguration(getContext());
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.login_button)
    protected void submit() {
        final String email = this.email.getEditText().getText().toString().trim();
        final String password = this.password.getEditText().getText().toString().trim();

        if (!validateEmail(email)) return;
        if (!validatePassword(password)) return;

        mAuthenticationActivity.getCompositeDisposable().add(new RxPermissions(this).request(Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        TelephonyManager telephonyManager = (TelephonyManager) mAuthenticationActivity.getSystemService(Context.TELEPHONY_SERVICE);
                        String deviceImei;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            deviceImei = telephonyManager.getImei();
                        else
                            deviceImei = telephonyManager.getDeviceId();

                        Token token = new Token();

                        token.setImei(deviceImei);
                        token.setEmail(email);
                        token.setPassword(password);


                        mAuthenticationActivity.getCompositeDisposable().add(mConnectionManager.getRetrofit()
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        retrofit -> {
                                            TokenService tokenService = retrofit.create(TokenService.class);
                                            mAuthenticationActivity.getCompositeDisposable().add(tokenService.getToken(token).subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeWith(new SingleCallbackWrapper<ApiResponse<Token>>() {
                                                        @Override
                                                        protected void onReceive(ApiResponse<Token> tokenApiResponse) {
                                                            Token token = tokenApiResponse.getData().get(0);
                                                            if (token != null) {
                                                                new AccountConfiguration(getActivity()).saveRefreshToken(token.getRefreshToken());
                                                                App.getInstance(mAuthenticationActivity.getApplicationContext()).setToken(token);
                                                            }
                                                        }
                                                    }));

                                            ServerService serverService = retrofit.create(ServerService.class);
                                            mAuthenticationActivity.getCompositeDisposable().add(serverService.getServerInfo().subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeWith(new SingleCallbackWrapper<ApiResponse<Server>>() {
                                                @Override
                                                protected void onReceive(ApiResponse<Server> serverApiResponse) {
                                                    Server server = serverApiResponse.getData().get(0);
                                                    if (server != null) {
                                                        mServerController.saveTorServerAddress(server.getTorAddress());
                                                    }

                                                    Navigation.findNavController(mView).navigate(R.id.home_activity);
                                                  //  Navigation.findNavController(mView).popBackStack(R.id.loginFragment, true);
                                                    mAuthenticationActivity.finish();
                                                }
                                            }));
                                        },
                                        error -> {
                                            if (error instanceof ConnectionException) {
                                                ConnectionException connectionException = (ConnectionException) error;
                                                Log.d("Error", getText(connectionException.getMessageId()).toString());
                                            }
                                        }));
                    } else {
                        Snackbar.make(mView, getString(R.string.permissions_not_granted), Snackbar.LENGTH_SHORT).show();
                    }
                }));
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            this.email.setError("Podaj email");
            return false;
        }

        if (!EmailValidator.getInstance().isValid(email)) {
            this.email.setError("Email jest nieprawidłowy");
            return false;
        }
        this.email.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            this.password.setError("Podaj hasło");
            return false;
        }
        this.password.setError(null);
        return true;
    }

    @Subscribe
    public void connectionEvent(ConnectionEvent connectionEvent) {
        if (connectionEvent.getData() == R.string.permissions_not_granted) {
            PermissionsUtil.askConnectionPermissions(this, mConnectionManager);
        }
    }

    @Subscribe
    public void requestExceptionEventHandler(RequestExceptionEvent event) {
        @RequestExceptionEvent.RequestExceptionType String exception = event.getRequestExceptionType();

        switch (exception) {
            case TIMEOUT: {
                Snackbar.make(mView, R.string.request_timeout, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case NETWORK_ERROR: {
                Snackbar.make(mView, R.string.network_api_error, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case UNKNOWN_API_ERROR:
                switch (event.getUnknownError()) {
                    case "USER_DOES_NOT_EXISTS": {
                        this.email.setError("Nie ma takiego użytkownika");
                        break;
                    }
                    case "INVALID_PASSWORD": {
                        this.password.setError("Błędne hasło");
                        break;
                    }
                    default:
                        Snackbar.make(mView, event.getUnknownError(), Snackbar.LENGTH_SHORT).show();
                }
            case UNKNOWN_ERROR: {
                Snackbar.make(mView, event.getUnknownError(), Snackbar.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAuthenticationActivity = ((AuthenticationActivity)context);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        mAuthenticationActivity.getCompositeDisposable().clear();
        super.onStop();
    }
}
