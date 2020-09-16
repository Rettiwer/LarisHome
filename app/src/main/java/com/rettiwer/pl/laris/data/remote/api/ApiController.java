package com.rettiwer.pl.laris.data.remote.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.rettiwer.pl.laris.App;
import com.rettiwer.pl.laris.BuildConfig;
import com.rettiwer.pl.laris.data.local.server.ServerConfiguration;
import com.rettiwer.pl.laris.data.remote.api.token.TokenService;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {
    private URL mBaseUrl;
    private ServerConfiguration mServerConfiguration;
    private int mTorPort;
    private Context mContext;

    public ApiController(ServerConfiguration serverConfiguration, Context mContext) {
        this.mServerConfiguration = serverConfiguration;
        this.mContext = mContext;
        buildBaseUrl();
    }

    public ApiController(ServerConfiguration serverConfiguration, int torPort, Context mContext) {
        this.mServerConfiguration = serverConfiguration;
        this.mTorPort = torPort;
        this.mContext = mContext;
        buildBaseUrl();
    }

    private void buildBaseUrl() {
        try {
            if (mTorPort != 0) {
                mBaseUrl = new URL("http", mServerConfiguration.getTorServerAddress(), 80, "/api/");

            }
            else {
                mBaseUrl = new URL("http", mServerConfiguration.getLocalServerAddress(), 80, "/api/");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Retrofit init() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        builder.addNetworkInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder request = original.newBuilder();
            request.addHeader("Content-Type", "application/json");
            return chain.proceed(request.build());
        });


        /*    builder.addNetworkInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder request = original.newBuilder();
                if (App.getInstance(mContext).getToken() != null) {
                    request.addHeader("Authorization", App.getInstance(mContext).getToken().getToken());
                    Log.d("a", App.getInstance(mContext).getToken().getToken());
                }
                return chain.proceed(request.build());
            });

*/
        if (mTorPort != 0) {
            InetSocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", mTorPort);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);

            builder.proxy(proxy).build();
        }

        builder.retryOnConnectionFailure(true);

        TokenServiceHolder tokenServiceHolder = new TokenServiceHolder(mContext);

        TokenAuthenticator authenticator = new TokenAuthenticator(tokenServiceHolder);
        builder.authenticator(authenticator);

        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.mBaseUrl.toString())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        TokenService tokenService = retrofit.create(TokenService.class);
        tokenServiceHolder.set(tokenService);

        return retrofit;
    }
}
