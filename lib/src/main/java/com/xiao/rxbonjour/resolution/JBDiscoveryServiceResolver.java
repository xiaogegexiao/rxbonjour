package com.xiao.rxbonjour.resolution;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
