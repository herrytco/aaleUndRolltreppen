package at.aau.group1.leiterspiel;

import org.junit.Test;

import at.aau.group1.leiterspiel.game.Ladder;
import at.aau.group1.leiterspiel.network.AckChecker;
import at.aau.group1.leiterspiel.network.Client;
import at.aau.group1.leiterspiel.network.ILobby;
import at.aau.group1.leiterspiel.network.MessageComposer;
import at.aau.group1.leiterspiel.network.MessageParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    @Test
    public void testClient() {
        Client client = new Client();
        try {
            client.writeOutput(null);
            client.connectToServer(null);
        } catch(Exception e) {
            assertTrue(false);
        }
    }

    @Test
    public void testAckChecker() {
        AckChecker ackChecker = new AckChecker();
        ackChecker.setLastAckID(1);
        assertTrue(ackChecker.waitForAcknowledgement(1));
        assertFalse(ackChecker.waitForAcknowledgement(0));
    }

    @Test
    public void testMessages() {
        int var = 0;

        ILobby lobby = new ILobby() {
            @Override
            public void ack(int id) {
                assertTrue(id == 37);
            }

            @Override
            public void joinLobby(int id, String name) {
                assertTrue(id == -1 && name.equals("Test"));
            }

            @Override
            public void assignIndex(int id, int index, String clientName) {
                assertTrue(id == 42 && clientName.equals("TestClient"));
            }

            @Override
            public void setPlayer(int id, int playerIndex, String playerType, String playerName) {
                assertTrue(id == 1 && playerIndex == 7 && playerType.equals("Test1") && playerName.equals("Test2"));
            }

            @Override
            public void allowCheats(int id, boolean permitCheats, int turnSkips) {
                assertTrue(id == 2 && !permitCheats && turnSkips == 2);
            }

            @Override
            public void setBoardType(int id, int type) {
                assertTrue(id == 3 && type == 4);
            }

            @Override
            public void startGame(int id) {
                assertTrue(id == 100);
            }
        };

        MessageComposer composer = new MessageComposer("Test", false);
        composer.setTest(true);
        MessageParser parser = new MessageParser();
        parser.registerLobby(lobby);

        composer.ack(37);
        parser.parseMessage(composer.getTestMessage());
        composer.joinLobby(-1);
        parser.parseMessage(composer.getTestMessage());
        composer.assignIndex(42, 0, "TestClient");
        parser.parseMessage(composer.getTestMessage());
        composer.setPlayer(1, 7, "Test1", "Test2");
        parser.parseMessage(composer.getTestMessage());
        composer.allowCheats(2, false, 2);
        parser.parseMessage(composer.getTestMessage());
        composer.setBoardType(3, 4);
        parser.parseMessage(composer.getTestMessage());
        composer.startGame(100);
        parser.parseMessage(composer.getTestMessage());

    }

}
