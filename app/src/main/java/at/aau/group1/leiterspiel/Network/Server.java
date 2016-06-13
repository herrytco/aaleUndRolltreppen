package at.aau.group1.leiterspiel.Network;

import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Created by Igor on 12.06.2016.
 */
public class Server {

    private final String TAG = "Server";

    private final int BLOCK_TIMEOUT = 50;
    private CommunicationTask communicator;
    private Socket socket;
    private ServerSocket serverSocket;
    private InputStream in;
    private OutputStream out;
    private StringBuilder sb;

    private boolean stop = false;
    private boolean write = false;
    private String output;
    private ICommListener listener;

    private MessageParser parser;

    public Server() {
        parser = new MessageParser();
    }

    public void registerListener(ICommListener listener) {
        this.listener = listener;
    }

    public void registerLobby(ILobby lobby) {
        parser.registerLobby(lobby);
    }

    public void registerOnlineGameManager(IOnlineGameManager ogm) { parser.registerOnlineGameManager(ogm); }

    /**
     * Initializes the socket on the specified port and starts the background communication.
     * @param serverSocket The ServerSocket initialized with a port number
     */
    public void startCommunication(ServerSocket serverSocket) {
        stop = false;
        this.serverSocket = serverSocket;
        // run the communication in background
        communicator = new CommunicationTask();
        communicator.execute();
    }

    public void disconnect() {
        if (socket != null) stop = true;
    }

    private void closeConnection() {
        try {
            in.close();
            out.flush();
            out.close();
            socket.close();
            socket = null;
            in = null;
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInput() {
        if (sb != null) {
            String str = sb.toString();
            sb = new StringBuilder();
            return str;
        }
        return null;
    }

    public void writeOutput(String str) {
        output = str;
        write = true;
    }

    /**
     * Upon establishing a connection, this AsyncTask continually listens to the active InputStream
     * and writes messages via OutputStream, until it gets stopped by calling disconnect().
     */
    public class CommunicationTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            sb = new StringBuilder();
            try {
                socket = serverSocket.accept(); // wait for a connection and open a socket for it
                in = socket.getInputStream();
                out = socket.getOutputStream();
                // set timeout for the read() method, otherwise the thread would be blocked until it receives something over the InputStream
                socket.setSoTimeout(BLOCK_TIMEOUT);

                byte buffer[] = new byte[1024];
                int readBytes = 0;
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

                while (!stop) {
                    // reading input
                    try {
                        readBytes = in.read(buffer);
                        if (readBytes != -1) {
                            sb.append(new String(buffer, 0, readBytes));
                            if (parser != null) {
                                Log.d(TAG, "parsing message: "+sb.toString());
                                // let the parser consume and process the message
                                parser.parseMessage(sb.toString());
                                sb = new StringBuilder();
                            } else {
                                // notify the listener that input was received
                                if (listener != null) listener.inputReceived();
                            }
                        }
                    } catch(SocketTimeoutException e) {
                        // do literally nothing and continue
                    }

                    // writing output
                    if (write && output != null) {
                        writer.write(output+"\n");
                        writer.flush(); // pro tip: this is important
                        write = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // disconnect
            closeConnection();

            return null;
        }
    }

}
