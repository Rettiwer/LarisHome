package com.rettiwer.pl.laris.data.remote.api.sensors;

import com.rettiwer.pl.laris.data.remote.api.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SensorService {

    @GET("room/{room-uuid}/devices")
    Call<ApiResponse<Sensor>> getSensors(@Path("room-uuid") String roomUuid);

    @POST("device")
    Call<ApiResponse<Sensor>> addDevice(@Body Sensor sensor);
}
