package com.xiao.rxbonjour.model;

import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;

import com.xiao.rxbonjour.utils.NsdUtils;


public class NsdServiceInfoWrapper implements NsdStatus {

    private final NsdServiceInfo nsdServiceInfo;
    @STATUS
    private final int status;

    public NsdServiceInfoWrapper(@NonNull NsdServiceInfo nsdServiceInfo,
                                 @STATUS int status) {
        this.nsdServiceInfo = nsdServiceInfo;
        this.status = status;
    }

    @Override
    public boolean isAdded() {
        return status == ADDED;
    }

    public NsdServiceInfo getNsdServiceInfo() {
        return nsdServiceInfo;
    }

    @STATUS
    public int getStatus() {
        return status;
    }

    public static NsdServiceInfoWrapper from(@NonNull NetworkServiceDiscoveryInfo source) {

        return new NsdServiceInfoWrapper(NsdUtils.from(source), NsdStatus.ADDED);
    }
}
