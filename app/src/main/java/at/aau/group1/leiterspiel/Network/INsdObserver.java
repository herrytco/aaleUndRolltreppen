package at.aau.group1.leiterspiel.Network;

import android.net.nsd.NsdServiceInfo;

/**
 * Created by Igor on 12.06.2016.
 */
public interface INsdObserver {

    public void notifyOfNewService(NsdServiceInfo serviceInfo);

}
