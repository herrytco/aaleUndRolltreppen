package at.aau.group1.leiterspiel.Network;

/**
 * Created by Igor on 14.06.2016.
 */
public class AckChecker {

    private final int TIMEOUT = 2000; // default timeout of 2 seconds
    private int lastAckID = -1;

    public AckChecker() {}

    public void setLastAckID(int id) { lastAckID = id; }

    /**
     * Waits for TIMEOUT ms for an acknowledgement with the specified ID.
     * If no correct ack message is received in time, return false.
     *
     * @param id ID of the expected ack message
     * @return true if the acknowledgement was successfully received
     */
    public boolean waitForAcknowledgement(int id) {
        // wait for ack
        long start = System.currentTimeMillis();
        while (lastAckID != id) {
            // if no acknowledgement comes in time, give up waiting
            if (System.currentTimeMillis() - start >= TIMEOUT) {
                return false;
            }
        }
        return true;
    }
}
