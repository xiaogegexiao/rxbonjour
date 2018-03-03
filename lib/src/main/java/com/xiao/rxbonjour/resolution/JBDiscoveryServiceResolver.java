package com.xiao.rxbonjour.resolution;

import android.content.Context;
import android.support.annotation.NonNull;

import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class JBDiscoveryServiceResolver implements Function<NsdServiceInfoWrapper, Observable<NsdServiceInfoWrapper>> {

    private WeakReference<Context> weakContext;

    public static JBDiscoveryServiceResolver with(@NonNull final Context context) {
        return new JBDiscoveryServiceResolver(new WeakReference<>(context));
    }

    private JBDiscoveryServiceResolver(@NonNull WeakReference<Context> weakContext) {
        this.weakContext = weakContext;
    }

    @Override
    public Observable<NsdServiceInfoWrapper> apply(NsdServiceInfoWrapper nsdServiceInfoWrapper) throws Exception {
        return Observable.create(JBResolverOnSubscribeEvent.from(weakContext.get(), nsdServiceInfoWrapper));
    }
}
