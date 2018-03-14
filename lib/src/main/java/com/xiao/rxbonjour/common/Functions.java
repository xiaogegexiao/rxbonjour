package com.xiao.rxbonjour.common;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;
import com.xiao.rxbonjour.model.NsdServiceInfoWrapper;

import io.reactivex.functions.Function;

/**
 * This class is the one that will allow the developers to apply function to the different
 * type of data
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class Functions {

    private static final Function<NsdServiceInfoWrapper, NetworkServiceDiscoveryInfo> nsdServiceWrapperConversion =
            new Function<NsdServiceInfoWrapper, NetworkServiceDiscoveryInfo>() {
                @Override
                public NetworkServiceDiscoveryInfo apply(NsdServiceInfoWrapper nsdServiceInfoWrapper) throws Exception {
                    return NetworkServiceDiscoveryInfo.from(nsdServiceInfoWrapper);
                }
            };

    /**
     * This method is the one that will transform the given {@link NsdServiceInfoWrapper} into a new
     * instance of {@link NetworkServiceDiscoveryInfo}
     * @return the {@link Function} needed in order to transform the different data types
     */
    public static Function<NsdServiceInfoWrapper, NetworkServiceDiscoveryInfo> toNetworkServiceDiscoveryInfo() {
        return nsdServiceWrapperConversion;
    }
}
