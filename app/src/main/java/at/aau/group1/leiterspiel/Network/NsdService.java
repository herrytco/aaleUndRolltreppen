package at.aau.group1.leiterspiel.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Igor on 12.06.2016.
 */
public class NsdService {

    public static final String SERVICE_NAME = "AuR_Game_";
    public static final String SERVICE_TYPE = "_http._tcp.";
    private ServerSocket serverSocket;

    private NsdManager.RegistrationListener registrationListener;
    private NsdManager nsdManager;
    private Context context;

    private String serverName;

    public NsdService(Context context, String serverName) {
        this.context = context;
        this.serverName = serverName;
    }

    public void startService() {
        int localPort = initializeServerSocket();
        initializeRegistrationListener();
        registerService(localPort);
    }

    public void stopService() {
        nsdManager.unregisterService(registrationListener);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Initialize socket and automatically get an available port.
     *
     * @return true if creating the socket was successful, otherwise false
     */
    public int initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.e("NsdService", "Failed creating server socket: "+e.getMessage());
        }
        return serverSocket.getLocalPort(); // return the chosen port.
    }

    /**
     * Registers the app's service in the local network.
     *
     * @param port The port used by the service
     */
    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME+serverName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        Log.d("NsdService", "Service "+serverName+" registered");
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("NsdService", "Registration failed. Error code: "+errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {

            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("NsdService", "Unregistration failed. Error code: "+errorCode);
            }
        };
    }

}
