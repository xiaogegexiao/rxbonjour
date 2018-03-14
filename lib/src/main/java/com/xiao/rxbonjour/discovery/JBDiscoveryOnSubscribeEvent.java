package com.xiao.rxbonjour.discovery;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.exceptions.NsdException;
import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;
import com.xiao.rxbonjour.model.NsdStatus;
import com.xiao.rxbonjour.utils.NsdUtils;

import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;

import static com.xiao.rxbonjour.exceptions.NsdException.START_DISCOVERY;
import static com.xiao.rxbonjour.exceptions.NsdException.STOP_DISCOVERY;
import static com.xiao.rxbonjour.model.NsdStatus.ADDED;
import static com.xiao.rxbonjour.model.NsdStatus.REMOVED;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class JBDiscoveryOnSubscribeEvent implements OnSubscribeEvent<NsdServiceInfoWrapper> {

    private NsdManager nsdManager;
    private final String protocol;
    private ObservableEmitter<? super NsdServiceInfoWrapper> emitter;


    public JBDiscoveryOnSubscribeEvent(@NonNull final Context context,
                                       @NonNull String protocol) {

        this.protocol = protocol;
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    private final Action dismissAction = new Action() {
        @Override
        public void run() throws Exception {
            if (nsdManager != null) {
                nsdManager.stopServiceDiscovery(discoveryListener);
                nsdManager = null;
            }
        }
    };

    private final NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            nsdManager.stopServiceDiscovery(this);
            emitter.onError(new NsdException(START_DISCOVERY, serviceType, errorCode));
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            nsdManager.stopServiceDiscovery(this);
            emitter.onError(new NsdException(STOP_DISCOVERY, serviceType, errorCode));
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {}

        @Override
        public void onDiscoveryStopped(String serviceType) {}

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            onNextRequested(serviceInfo, ADDED);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            onNextRequested(serviceInfo, REMOVED);
        }
    };

    private void onNextRequested(NsdServiceInfo serviceInfo, @NsdStatus.STATUS int status) {
        if (!emitter.isDisposed()) {
            emitter.onNext(new NsdServiceInfoWrapper(serviceInfo, status));
        }
    }

    @Override
    public Action onCompleted() {
        return dismissAction;
    }

    @Override
    public void subscribe(ObservableEmitter<NsdServiceInfoWrapper> emitter) throws Exception {
        this.emitter = emitter;
        if (!NsdUtils.isValidProtocol(protocol)) {
            emitter.onError(new NsdException());
        } else {
            nsdManager.discoverServices(protocol, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            emitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    dismissAction.run();
                }
            });
        }
    }
}
