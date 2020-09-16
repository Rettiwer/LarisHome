package com.rettiwer.pl.laris.data.remote.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.R;
import com.rettiwer.pl.laris.data.local.user.AccountConfiguration;
import com.rettiwer.pl.laris.data.remote.ConnectionEvent;
import com.rettiwer.pl.laris.data.remote.api.token.Token;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;

import javax.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class TokenAuthenticator implements Authenticator {
    private TokenServiceHolder mTokenServiceHolder;

    TokenAuthenticator(TokenServiceHolder tokenServiceHolder) {
        this.mTokenServiceHolder = tokenServiceHolder;
    }

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {
        AccountConfiguration accountConfiguration = new AccountConfiguration(mTokenServiceHolder.getContext());

        String error = getErrorMessage(response.body());
        Token token = App.getInstance(mTokenServiceHolder.getContext()).getToken();
        if (!error.equals("INVALID_TOKEN") && token != null) {
            return response.request().newBuilder()
                    .header(AUTHORIZATION, token.getToken())
                    .build();
        }


        token = new Token();
        token.setRefreshToken(accountConfiguration.getRefreshToken());

        retrofit2.Response<ApiResponse<Token>> tokenResponse = mTokenServiceHolder.get().updateToken(token).execute();

        if (!tokenResponse.body().getStatus()) {
            EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.UNKNOWN_ERROR, tokenResponse.body().getMessage()));
        }

        Token newToken = tokenResponse.body().getData().get(0);
        App.getInstance(mTokenServiceHolder.getContext()).setToken(newToken);

        return response.request().newBuilder()
                .header(AUTHORIZATION, newToken.getToken())
                .build();
    }

    private String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
