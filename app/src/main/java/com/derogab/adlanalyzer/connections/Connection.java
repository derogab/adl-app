package com.derogab.adlanalyzer.connections;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    private static final String TAG = "Connection";

    // Server information
    private String destination;
    private int port;
    // Connection socket
    private Socket socket;
    // Server message
    private String serverMessage;
    // Check variable
    private boolean mRun = false; // while this is true, the server will continue running
    // Buffer used to send messages
    private PrintWriter bufferOut;
    // Buffer used to read messages from the server
    private BufferedReader bufferIn;

    // Callbacks
    private OnMessageReceivedListener onMessageReceivedListener;
    private OnConnectionSuccessListener onConnectionSuccessListener;
    private OnConnectionErrorListener onConnectionErrorListener;

    /**
     * Constructor of the class.
     * OnMessagedReceived listens for the messages received from server
     * OnMessagedReceived listens for the messages received from server
     * OnMessagedReceived listens for the messages received from server
     *
     * @param dest the destination (ip/url) of the server
     * @param port the destination port of the server
     * @param onMessageReceivedListener the OnMessageReceived listener / callback method
     * @param onConnectionErrorListener the OnConnectionErrorListener listener / callback method
     * @param onConnectionSuccessListener the OnConnectionSuccessListener listener / callback method
     */
    public Connection(String dest, int port,
                      OnMessageReceivedListener onMessageReceivedListener,
                      OnConnectionErrorListener onConnectionErrorListener,
                      OnConnectionSuccessListener onConnectionSuccessListener) {

        this.destination = dest;
        this.port = port;
        this.onMessageReceivedListener = onMessageReceivedListener;
        this.onConnectionErrorListener = onConnectionErrorListener;
        this.onConnectionSuccessListener = onConnectionSuccessListener;

    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {

        if (bufferOut != null) {
            try {
                bufferOut.println(message);
                bufferOut.flush();
            }
            catch (NullPointerException e) {
                Log.e(TAG, "Send message error: connection already closed.");
            }
            catch (Exception e) {
                Log.e(TAG, "Error: " + e);
            }
        }

    }

    /**
     * Close the connection and release the members
     */
    public void close() {

        mRun = false;

        if (bufferOut != null) {
            try {
                bufferOut.flush();
                bufferOut.close();
            }
            catch (Exception e) {
                Log.e(TAG, "Buffer flush error: probably connection already closed.");
            }


        }

        socket = null;

        onMessageReceivedListener = null;
        onConnectionSuccessListener = null;
        onConnectionErrorListener = null;
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
        destination = null;
        port = 0;
    }

    /**
     * Tasks when the connection is established
     */
    public void run() {

        mRun = true;

        try {
            Log.d(TAG, "Connecting to server...");
            // Create a socket to make the connection with the server
            Log.d(TAG, "Host: " + destination);
            Log.d(TAG, "Port: " + port);
            socket = new Socket(destination, port);
            // Exec callback after connection
            if (onConnectionSuccessListener != null)
                onConnectionSuccessListener.onConnectionSuccess();

        } catch (Exception e) {
            // Exec callback
            if (onConnectionErrorListener != null)
                onConnectionErrorListener.onConnectionError();
        }

        if (socket != null) {

            try {

                // Sends the message to the server
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                // Receives the message which the server sends back
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // In this while the client listens for the messages sent by the server
                while (mRun) {

                    serverMessage = bufferIn.readLine();

                    if (serverMessage != null && onMessageReceivedListener != null) {

                        if (onMessageReceivedListener != null)
                            onMessageReceivedListener.messageReceived(serverMessage);

                    }

                }

            } catch (Exception e) {

                // Error communicating with the server
                Log.e(TAG, "Receiving message error: probably connection already closed.");

            } finally {

                if (socket != null) {

                    try {
                        // The socket must be closed.
                        // It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        socket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket close error: probably connection already closed.");
                    }

                }

            }

        }

    }

    /**
     * Interface to receive messages
     * OnMessageReceivedListener listens for message received
     *
     */
    public interface OnMessageReceivedListener {
        public void messageReceived(String message);
    }

    /**
     * Interface to receive connection errors
     * OnConnectionErrorListener listens for the connection error
     *
     */
    public interface OnConnectionErrorListener {
        void onConnectionError();
    }

    /**
     * Interface to receive connection success
     * OnConnectionSuccessListener listens for the connection success
     *
     */
    public interface OnConnectionSuccessListener {
        void onConnectionSuccess();
    }

}