package com.xiao.rxbonjour.advertise;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.exceptions.NsdException;
import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;
import com.xiao.rxbonjour.utils.NsdUtils;

import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;


public class CompatAdvertiseOnSubscribeEvent implements OnSubscribeEvent<NetworkServiceDiscoveryInfo> {

    private ServiceInfo serviceInfo;
    private final NetworkServiceDiscoveryInfo nsdServiceInfo;
    private JmDNS jmDNS;

    public CompatAdvertiseOnSubscribeEvent(@NonNull String serviceName,
                                           @NonNull String serviceLayer,
                                           int servicePort,
                                           @Nullable Map<String, String> attributes) {

        serviceInfo = ServiceInfo.create(serviceLayer, serviceName, servicePort, 0, 0, attributes);
        nsdServiceInfo = NetworkServiceDiscoveryInfo.from(serviceInfo, attributes);
    }

    @Override
    public Action onCompleted() {
        return dismissAction;
    }

    private final Action dismissAction = new Action() {
        @Override
        public void run() throws Exception {
            jmDNS.unregisterService(serviceInfo);
            jmDNS.unregisterAllServices();
            serviceInfo = null;
        }
    };

    private void startSubscription(ObservableEmitter<NetworkServiceDiscoveryInfo> emitter) {
        try {
            jmDNS = JmDNS.create();
            jmDNS.registerService(serviceInfo);
            emitter.onNext(nsdServiceInfo);
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    dismissAction.run();
                }
            });
        } catch (Exception e) {
            emitter.onError(e);
        }
    }

    @Override
    public void subscribe(ObservableEmitter<NetworkServiceDiscoveryInfo> emitter) throws Exception {
        if (emitter.isDisposed()) {
            return;
        }
        if (!NsdUtils.isValidProtocol(serviceInfo.getProtocol())) {
            emitter.onError(new NsdException());
        } else {
            startSubscription(emitter);
        }
    }
}
