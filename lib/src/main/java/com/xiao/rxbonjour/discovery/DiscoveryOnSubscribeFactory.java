package com.xiao.rxbonjour.discovery;


import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.xiao.rxbonjour.RxBonjour;
import com.xiao.rxbonjour.common.Functions;
import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.common.Transformers;
import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;
import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;
import com.xiao.rxbonjour.resolution.JBDiscoveryServiceResolver;

import io.reactivex.Observable;

public class DiscoveryOnSubscribeFactory {

    public static Observable<NetworkServiceDiscoveryInfo> from(@NonNull Context context) {
        return buildNewJBObservableFrom(context, RxBonjour.ALL_AVAILABLE_SERVICES)
                .map(Functions.toNetworkServiceDiscoveryInfo())
                .compose(Transformers.networking());
    }

    public static Observable<NetworkServiceDiscoveryInfo> from(@NonNull Context context,
                                                               @NonNull String protocol) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return buildJBObservableFrom(context, protocol);
        } else {
            return buildCompatObservableFrom(context, protocol);
        }
    }

    public static Observable<NetworkServiceDiscoveryInfo> from(@NonNull Context context,
                                                               @NonNull String protocol,
                                                               boolean needsTxtRecord) {

        if (needsTxtRecord) {
            return buildCompatObservableFrom(context, protocol);
        } else {
            return from(context, protocol);
        }
    }

    private static Observable<NetworkServiceDiscoveryInfo> buildJBObservableFrom(Context context, String protocol) {
        return buildNewJBObservableFrom(context, protocol)
                .concatMap(JBDiscoveryServiceResolver.with(context))
                .map(Functions.toNetworkServiceDiscoveryInfo())
                .compose(Transformers.networking());
    }

    private static Observable<NsdServiceInfoWrapper> buildNewJBObservableFrom(Context context, String protocol) {
        OnSubscribeEvent<NsdServiceInfoWrapper> onSubscribe = new JBDiscoveryOnSubscribeEvent(context, protocol);
        return Observable.create(onSubscribe);

    }
    private static Observable<NetworkServiceDiscoveryInfo> buildCompatObservableFrom(Context context, String protocol) {
        OnSubscribeEvent<NetworkServiceDiscoveryInfo> onSubscribe = new CompatOnSubscribeEvent(context, protocol);
        return Observable.create(onSubscribe).doOnComplete(onSubscribe.onCompleted()).compose(Transformers.networking());
    }
}
