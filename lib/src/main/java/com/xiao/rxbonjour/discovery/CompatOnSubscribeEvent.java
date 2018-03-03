package com.xiao.rxbonjour.discovery;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.exceptions.NsdException;
import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;
import com.xiao.rxbonjour.utils.NsdUtils;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;

import static com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo.from;
import static com.xiao.rxbonjour.model.NsdStatus.ADDED;
import static com.xiao.rxbonjour.model.NsdStatus.REMOVED;

public class CompatOnSubscribeEvent implements OnSubscribeEvent<NetworkServiceDiscoveryInfo> {

    private JmDNS jmDNS;
    private ObservableEmitter<? super NetworkServiceDiscoveryInfo> emitter;
    private final String protocol;
    private Context context;
    private final String SUFFIX = "local.";

    private final Action dismissAction = new Action() {
        @Override
        public void run() throws Exception {
            if (jmDNS != null) {
                try {
                    jmDNS.removeServiceListener(protocol, listener);
                    jmDNS.close();
                } catch (Exception e) {}
            }
        }
    };

    private ServiceListener listener = new ServiceListener() {
        @Override
        public void serviceAdded(ServiceEvent event) {
            event.getDNS().requestServiceInfo(event.getType(), event.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent serviceEvent) {
            if (!emitter.isDisposed()) {
                emitter.onNext(from(serviceEvent.getInfo(), REMOVED));
            }
        }

        @Override
        public void serviceResolved(ServiceEvent serviceEvent) {
            if (!emitter.isDisposed()) {
                emitter.onNext(from(serviceEvent.getInfo(), ADDED));
            }
        }
    };

    public CompatOnSubscribeEvent(@NonNull Context context, @NonNull String protocol) {
        this.protocol = buildProtocolFrom(protocol);
        this.context = context;
    }

    private String buildProtocolFrom(String protocol) {
        if (!protocol.endsWith(SUFFIX)) {
            return addSuffixTo(protocol);
        } else {
            return protocol;
        }
    }

    @NonNull
    private String addSuffixTo(String protocol) {
        if (!protocol.endsWith(".")) {
            return protocol + "." + SUFFIX;
        }
        return protocol + SUFFIX;
    }

    @Override
    public Action onCompleted() {
        return dismissAction;
    }

    private void startSubscription(ObservableEmitter<? super NetworkServiceDiscoveryInfo> emitter) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            final InetAddress inetAddress = buildAddress(wifiManager);
            jmDNS = JmDNS.create(inetAddress, inetAddress.toString());
            jmDNS.addServiceListener(protocol, listener);
        } catch (IOException e) {
            emitter.onError(e);
        }

        emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                dismissAction.run();
            }
        });
    }

    private InetAddress buildAddress(WifiManager wifiManager) throws IOException {
        int baseAddress = wifiManager.getConnectionInfo().getIpAddress();
        byte[] converted = new byte[] { (byte) (baseAddress & 0xff), (byte) (baseAddress >> 8 & 0xff),
                (byte) (baseAddress >> 16 & 0xff), (byte) (baseAddress >> 24 & 0xff) };

        return InetAddress.getByAddress(converted);
    }

    @Override
    public void subscribe(ObservableEmitter<NetworkServiceDiscoveryInfo> emitter) throws Exception {
        this.emitter = emitter;

        if (!NsdUtils.isValidProtocol(protocol)) {
            emitter.onError(new NsdException());
        } else {
            startSubscription(emitter);
        }
    }
}
