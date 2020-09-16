package com.rettiwer.pl.laris.data.remote.api;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.reactivex.observers.DisposableSingleObserver;
import okhttp3.ResponseBody;

public abstract class SingleCallbackWrapper <T extends BaseResponse> extends DisposableSingleObserver<T> {

    public SingleCallbackWrapper() { }

    protected abstract void onReceive(T t);

    @Override
    public void onSuccess(T t) {
        onReceive(t);
    }

    @Override
    public void onError(Throwable t) {
        if (t instanceof HttpException) {
            ResponseBody responseBody = ((HttpException) t).response().errorBody();
            String errorMessage = getErrorMessage(responseBody);
            switch (errorMessage) {
                case "INVALID_TOKEN":
                    EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.INVALID_TOKEN));
                default:
                    EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.UNKNOWN_API_ERROR, errorMessage));
            }
        } else if (t instanceof SocketTimeoutException) {
            EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.TIMEOUT));
        } else if (t instanceof IOException) {
            EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.NETWORK_ERROR));
        } else {
            EventBus.getDefault().post(new RequestExceptionEvent(RequestExceptionEvent.UNKNOWN_ERROR, t.getMessage()));

        }
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