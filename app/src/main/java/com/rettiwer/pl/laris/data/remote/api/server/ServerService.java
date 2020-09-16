package com.rettiwer.pl.laris.data.remote.api.server;

import com.rettiwer.pl.laris.data.remote.api.ApiResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerService {
    @GET("server")
    Single<ApiResponse<Server>> getServerInfo();

    @POST("server")
    Single<ApiResponse<Server>> getServerStatus();
}
