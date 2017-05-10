package com.digital_mystic.localchat;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Jonathan on 5/7/2017.
 */

public class NsdHelper {

    NsdManager nsdManager;
    NsdManager.RegistrationListener registrationListener;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.ResolveListener resolveListener;

    NsdServiceInfo mServiceInfo;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NsdHelper";
    public static String serviceBaseName = "LocalChat";
    public static String serviceName;



    public NsdHelper(Context context){

        if(nsdManager == null){
            nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }

        //initialize various listeners for
        initializeDiscoveryListener();

        //Create a unique servicename for this device;
        serviceName = serviceBaseName + ":" + Build.MODEL;

    }

    public void initializeDiscoveryListener(){
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "onStartDiscoveryFailed");

            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "onStopDiscoveryFailed");

            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "onDiscoveryStarted" +serviceType);

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "onDiscoveryStopped ");

            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceFound " +serviceInfo.getServiceName());
                if(serviceInfo.getServiceName().equals(serviceName)){
                    Log.d(TAG,"Same IP");
                }else if(serviceInfo.getServiceName().contains(serviceBaseName)){
                    serviceName = serviceInfo.getServiceName();
                    if(resolveListener == null){
                        initializeResolverListener();
                    }
                    nsdManager.resolveService(serviceInfo,resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceLost " +serviceInfo.getServiceName());
                if(serviceInfo == mServiceInfo){ //Check if the lost service was one we care about
                    mServiceInfo = null;
                }
            }
        };


    }

    public void initializedRegistrationListener(){
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onRegistrationFailed");

            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onUnregistrationFailed");

            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceRegistered");
                //Set the service name after alteration to prevent conflicts
                serviceName = serviceInfo.getServiceName();

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "onServiceUnregistered");

            }
        };

    }

    public void initializeResolverListener(){
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "onServiceResolved" + serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "onServiceResolved" + serviceInfo);
                if (serviceInfo.getServiceName().equals(serviceName)) {
//                    Log.d(TAG, "Same IP.");
//                    return;
                }
                mServiceInfo = serviceInfo;
            }
        };



    }

    public void registerService(int port){
        shutdown();
        initializedRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        nsdManager.registerService(serviceInfo,NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    /**
     * Begin discovering services registered on the network.
     * Discovery continues until explicitly stopped.
     */
    public void discoverServices(){
        if(discoveryListener == null){
            initializeDiscoveryListener();
        }
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    /**
     * Stop NsdManager from listening for new network services.
     */
    public void stopServiceDiscovery(){
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public NsdServiceInfo getSelectedService() { return mServiceInfo;}

    public void shutdown(){
        if(registrationListener != null){
            try {
                nsdManager.unregisterService(registrationListener);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
        registrationListener = null;

    }
}
