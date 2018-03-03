package com.xiao.rxbonjour;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xiao.rxbonjour.advertise.AdvertiseOnSubscribeFactory;
import com.xiao.rxbonjour.common.OnSubscribeEvent;
import com.xiao.rxbonjour.common.Transformers;
import com.xiao.rxbonjour.discovery.DiscoveryOnSubscribeFactory;
import com.xiao.rxbonjour.exceptions.NsdException;
import com.xiao.rxbonjour.model.NetworkServiceDiscoveryInfo;

import java.util.Map;

import io.reactivex.Observable;

/**
 * This class is the one entry point for the library.
 *
 * @see #advertise(Context, String, String, int, Map) Advertising a new service
 */
public class RxBonjour {

    public static final String ALL_AVAILABLE_SERVICES = "_services._dns-sd._udp";

    private RxBonjour() {
        throw new IllegalStateException();
    }

    /**
     * This method is the one used in order to advertise the service on the network. As per default,
     * this call will be executed on a proper Scheduler and return its result onto the main thread.
     *
     * @param context      needed in order to retrieve the service for native API
     * @param serviceName  the name of the service that will be advertised on the network
     * @param serviceLayer the type of service that will be served
     * @param servicePort  the port on which the service will be available
     * @param attributes   the additional attributes that will be passed from the server
     * @return an {@link Observable} that will emit the {@link NetworkServiceDiscoveryInfo} as soon
     * as the service is correctly started.
     */
    public static Observable<NetworkServiceDiscoveryInfo> advertise(@NonNull Context context,
                                                                    @NonNull String serviceName,
                                                                    @NonNull String serviceLayer,
                                                                    int servicePort,
                                                                    @Nullable Map<String, String> attributes) {

        OnSubscribeEvent<NetworkServiceDiscoveryInfo> onSubscribe = AdvertiseOnSubscribeFactory.from(context, serviceName, serviceLayer,
                servicePort, attributes);

        return Observable.create(onSubscribe).doOnComplete(onSubscribe.onCompleted()).compose(Transformers.networking());
    }

    /**
     * This method is the one that shall be used in order to discover all the services available in
     * the current network. By default, this call will be executed on a proper Scheduler and return
     * the results onto the main thread.<br/>
     * <p/>
     * <b>Note</b>: as per API limitation, listing all the services available in the current network
     * will not allow the components to resolve them. That means that you will have to call the
     * {@link #startDiscovery(Context, String)} on the specific protocol for having the data resolved
     * correctly. If, when doing so you receive an exception from the {@link android.net.nsd.NsdManager.DiscoveryListener},
     * make sure you closed the previous {@link io.reactivex.disposables.Disposable} before starting a new one.<br/>
     * <p/>
     * <b>Note</b>: this method is making use of the {@link #ALL_AVAILABLE_SERVICES} constant to discover
     * all the services in the available network
     *
     * @param context needed in order to retrieve the service for native API
     * @return an {@link Observable} that will emit all the different services found on the current
     * network
     */
    public static Observable<NetworkServiceDiscoveryInfo> startDiscovery(@NonNull Context context) {
        return DiscoveryOnSubscribeFactory.from(context);
    }

    /**
     * This method is the one that shall be used in order to discover all the services available in
     * the current network for a given protocol. By default, this call will be executed on a proper
     * Scheduler and return the results onto the main thread.<br/>
     *
     * @param context  needed in order to retrieve the service for native API
     * @param protocol the requested protocol
     * @return an {@link Observable} that will emit all the instances of the service matching the
     * given protocol found on the current network
     * @throws NsdException if {@code protocol} equals {@link #ALL_AVAILABLE_SERVICES} or the
     *                      "_services._dns-sd._udp" value
     */
    public static Observable<NetworkServiceDiscoveryInfo> startDiscovery(
            @NonNull Context context, @NonNull String protocol) throws NsdException {

        if (protocol.equalsIgnoreCase(ALL_AVAILABLE_SERVICES)) {
            throw new NsdException(NsdException.INVALID_ARGUMENT, protocol, 0);
        }

        return DiscoveryOnSubscribeFactory.from(context, protocol);
    }

    /**
     * This method is the one that shall be used in order to discover all the services available in
     * the current network for a given protocol. By default, this call will be executed on a proper
     * Scheduler and return the results onto the main thread.<br/>
     *
     * @param context        needed in order to retrieve the service for native API
     * @param protocol       the requested protocol
     * @param needsTxtRecord specifies if the discovery needs to use custom attributes
     * @return an {@link Observable} that will emit all the instances of the service matching the
     * given protocol found on the current network
     * @throws NsdException if {@code protocol} equals {@link #ALL_AVAILABLE_SERVICES} or the
     *                      "_services._dns-sd._udp" value
     */
    public static Observable<NetworkServiceDiscoveryInfo> startDiscovery(
            @NonNull Context context, @NonNull String protocol, boolean needsTxtRecord) throws NsdException {

        if (protocol.equalsIgnoreCase(ALL_AVAILABLE_SERVICES)) {
            throw new NsdException(NsdException.INVALID_ARGUMENT, protocol, 0);
        }

        return DiscoveryOnSubscribeFactory.from(context, protocol, needsTxtRecord);
    }

}
