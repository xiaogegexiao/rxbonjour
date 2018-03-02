package com.xiao.rxbonjour.common;

import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * This class is the one that is used in order to provide {@link io.reactivex.ObservableTransformer}s to
 * the library
 */
public class Transformers {

    private static final ObservableTransformer<NetworkServiceDiscoveryInfo, NetworkServiceDiscoveryInfo> schedulerTransformer =
            new ObservableTransformer<NetworkServiceDiscoveryInfo, NetworkServiceDiscoveryInfo>() {
                @Override
                public ObservableSource<NetworkServiceDiscoveryInfo> apply(Observable<NetworkServiceDiscoveryInfo> upstream) {
                    return upstream.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                }
            };

    /**
     * This method is the one that, used with the {@link Observable#compose(ObservableTransformer)}
     * operator, will allow the target {@link Observable} to be executed on a proper Scheduler
     * and return its result onto the main thread.
     * @return the {@link io.reactivex.ObservableTransformer} needed for threading purposes
     */
    public static ObservableTransformer<NetworkServiceDiscoveryInfo, NetworkServiceDiscoveryInfo> networking() {
        return schedulerTransformer;
    }
}
