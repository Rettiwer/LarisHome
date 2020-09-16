package com.rettiwer.pl.laris.data.remote.api.room;

import com.rettiwer.pl.laris.data.remote.api.ApiResponse;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RoomService {
    @GET("room")
    Single<ApiResponse<Room>> getRooms();

    @GET("room/{uuid}")
    Single<ApiResponse<Room>> getRoom(@Path("uuid") String uuid);

    @POST("room")
    Single<ApiResponse<Room>> addRoom(@Body Room room);
}
