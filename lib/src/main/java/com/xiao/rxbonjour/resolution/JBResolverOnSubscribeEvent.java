package com.xiao.rxbonjour.resolution;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;


import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static android.net.nsd.NsdManager.ResolveListener;


public class JBResolverOnSubscribeEvent implements ObservableOnSubscribe<NsdServiceInfoWrapper> {

    private NsdServiceInfoWrapper nsdServiceInfo;
    private NsdManager nsdManager;
    private ObservableEmitter<? super NsdServiceInfoWrapper> emitter;

    public static JBResolverOnSubscribeEvent from (@NonNull final Context context,
                                                   @NonNull final NsdServiceInfoWrapper nsdServiceInfo) {

        return new JBResolverOnSubscribeEvent(context, nsdServiceInfo);
    }

    private JBResolverOnSubscribeEvent(@NonNull Context context,
                                       @NonNull NsdServiceInfoWrapper nsdServiceInfo) {

        this.nsdServiceInfo = nsdServiceInfo;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    private final ResolveListener resolveListener = new ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            if (!emitter.isDisposed()) {
                emitter.onNext(new NsdServiceInfoWrapper(serviceInfo, nsdServiceInfo.getStatus()));
                emitter.onComplete();
            }
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            if (!emitter.isDisposed()) {
                emitter.onNext(new NsdServiceInfoWrapper(serviceInfo, nsdServiceInfo.getStatus()));
                emitter.onComplete();
            }
        }
    };

    @Override
    public void subscribe(ObservableEmitter<NsdServiceInfoWrapper> emitter) throws Exception {
        this.emitter = emitter;
        nsdManager.resolveService(nsdServiceInfo.getNsdServiceInfo(), resolveListener);
    }
}
