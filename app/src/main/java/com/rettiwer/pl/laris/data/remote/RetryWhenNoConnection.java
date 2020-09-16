package com.rettiwer.pl.laris.data.remote;

import com.rettiwer.pl.laris.R;

import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class RetryWhenNoConnection implements Function<Flowable<Throwable>, Publisher<?>> {

    @Override
    public Publisher<?> apply(Flowable<Throwable> throwableFlowable) throws Exception {
        return throwableFlowable.flatMap(error -> {
            if (error instanceof ConnectionException) {
                ConnectionException connectionException = ((ConnectionException) error);
                if (connectionException.getMessageId() == R.string.connecting_to_tor_network) {
                    EventBus.getDefault().post(new ConnectionEvent(R.string.connecting_to_tor_network));
                    return Flowable.timer(1, TimeUnit.SECONDS);
                }
                else if (connectionException.getMessageId() == R.string.no_connection) {
                    EventBus.getDefault().post(new ConnectionEvent(R.string.no_connection));
                    return Flowable.timer(1, TimeUnit.SECONDS);
                }
            }
            return Flowable.error(error);
        });
    }
}