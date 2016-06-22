package at.aau.group1.leiterspiel.network;

import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Created by Igor on 12.06.2016.
 */
public class Client {

    private static final String TAG = "Client";

    private static final int BLOCK_TIMEOUT = 50;
    private CommunicationTask communicator;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private StringBuilder sb;

    private boolean stop = false;
    private boolean write = false;
    private String output;
    private ICommListener listener;

    private MessageParser parser;

    public Client() {
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
     * Attempts to establish a connection to the server instance of the app.
     * @param serviceInfo The NsdServiceInfo of the service that this Client should connect to
     */
    public void connectToServer(NsdServiceInfo serviceInfo) {
        if (socket != null)
            Log.d(TAG, "Connection not established: previous connection is still active.");
        stop = false;
        SocketAddress address = new InetSocketAddress(
                serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());

        socket = new Socket();

        // start the background communication
        communicator = new CommunicationTask();
        communicator.execute(address);
    }

    public void disconnect() {
        if (socket != null) stop = true;
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
    public class CommunicationTask extends AsyncTask<SocketAddress,Void,Void> {

        @Override
        protected Void doInBackground(SocketAddress... params) {
            sb = new StringBuilder();
            try {
                SocketAddress address = params[0];
                socket.connect(address, 5000);
                if (socket.isConnected()) {
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    // set timeout for the read() method, otherwise the thread would be blocked until it receives something over the InputStream
                    socket.setSoTimeout(BLOCK_TIMEOUT);
                } else return null;

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

                while (!stop) {
                    // reading input
                    readInput(in);

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

        private void readInput(InputStream in) {
            byte[] buffer = new byte[1024];
            int readBytes;
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
                        if (listener != null)
                            listener.inputReceived();
                    }
                }
            } catch(SocketTimeoutException e) {
                // do literally nothing and continue
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
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
    }

}
