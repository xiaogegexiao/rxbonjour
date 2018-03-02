package com.xiao.rxbonjour.advertise;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;

import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.exceptions.NsdException;
import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;
import com.xiao.rxbonjour.utils.NsdUtils;

import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;


public class JBAdvertiseOnSubscribeEvent implements
        NsdManager.RegistrationListener, OnSubscribeEvent<NetworkServiceDiscoveryInfo> {

    protected NsdServiceInfo nsdServiceInfo;
    private Context context;
    private ObservableEmitter<? super NetworkServiceDiscoveryInfo> emitter;
    private NsdManager nsdManager;

    public JBAdvertiseOnSubscribeEvent(@NonNull Context context,
                                       @NonNull String serviceName,
                                       @NonNull String serviceLayer,
                                       int servicePort) {

        nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceName(serviceName);
        nsdServiceInfo.setServiceType(serviceLayer);
        nsdServiceInfo.setPort(servicePort);

        this.context = context;
    }

    private final Action dismissAction = new Action() {
        @Override
        public void run() throws Exception {
            dismiss();
        }
    };

    private void dismiss() {
        if (nsdManager != null) {
            nsdManager.unregisterService(this);
        }
    }

    @Override
    public Action onCompleted() {
        return dismissAction;
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        onError();
    }

    private void onError() {
        if (!emitter.isDisposed()) {
            emitter.onError(new IllegalStateException());
        }
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        onError();
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        if (!emitter.isDisposed()) {
            emitter.onNext(NetworkServiceDiscoveryInfo.from(serviceInfo));
        }
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
        context = null;
        nsdManager = null;
    }

    @Override
    public void subscribe(ObservableEmitter<NetworkServiceDiscoveryInfo> emitter) throws Exception {
        this.emitter = emitter;
        if (!NsdUtils.isValidProtocol(nsdServiceInfo.getServiceType())) {
            emitter.onError(new NsdException());
        } else {
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, this);
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    dismissAction.run();
                }
            });
        }
    }
}
