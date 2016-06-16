package at.aau.group1.leiterspiel.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Igor on 12.06.2016.
 */
public class NsdDiscovery {

    private final String TAG = "NsdDiscovery";
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private Context context;

    private INsdObserver iNsdObserver;
    private ArrayList<NsdServiceInfo> discoveredServices = new ArrayList<NsdServiceInfo>();

    private String clientName;

    public NsdDiscovery(Context context, String clientName) {
        this.context = context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.clientName = clientName;
    }

    public void registerObserver(INsdObserver observer) {
        iNsdObserver = observer;
    }

    public void startDiscovery() {
        initResolveListener();
        initializeDiscoveryListener();

        nsdManager.discoverServices(
                NsdService.SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener);
    }

    public void stopDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    private void initResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // error code 3: "Indicates that the operation failed because it is already active."
                Log.d(TAG, "Resolve failed: "+errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                discoveredServices.add(serviceInfo);
                if (iNsdObserver!=null) iNsdObserver.notifyOfNewService(serviceInfo);
            }
        };
    }

    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(NsdService.SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(NsdService.SERVICE_NAME+clientName)) {
                    Log.d(TAG, "Same machine: " + NsdService.SERVICE_NAME+clientName);
                } else if (service.getServiceName().contains(NsdService.SERVICE_NAME)){
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
               nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code: " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }
}
