package com.rettiwer.pl.laris.data.remote.api.user;


import com.rettiwer.pl.laris.data.remote.api.ApiResponse;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface UserService {
    @GET("user")
    Single<ApiResponse<User>> getUsers();
}
