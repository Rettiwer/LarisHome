package com.rettiwer.pl.laris.data.remote.api.token;

import com.rettiwer.pl.laris.data.remote.api.ApiResponse;
import com.rettiwer.pl.laris.data.remote.api.user.User;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface TokenService {
    @POST("token")
    Single<ApiResponse<Token>> getToken(@Body Token token);

    @PUT("token")
    Call<ApiResponse<Token>> updateToken(@Body Token token);
}
