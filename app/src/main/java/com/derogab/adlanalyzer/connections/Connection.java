package com.derogab.adlanalyzer.connections;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    private static final String TAG = "Connection";

    // server information
    private String mServerDestination;
    private int mServerPort;
    // connection socket
    private Socket mSocket;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class.
     * OnMessagedReceived listens for the messages received from server
     *
     * @param dest the destination (ip/url) of the server
     * @param port the destination port of the server
     * @param listener the OnMessageReceived listener / callback method
     */
    public Connection(String dest, int port, OnMessageReceived listener) {
        mServerDestination = dest;
        mServerPort = port;
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stop() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        mServerDestination = null;
        mServerPort = 0;
        mSocket = null;
    }

    public void run() {

        mRun = true;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    Log.d(TAG, "C: Connecting...");

                    // create a socket to make the connection with the server
                    Log.d(TAG, "Host: " + mServerDestination);
                    Log.d(TAG, "Port: " + mServerPort);
                    mSocket = new Socket(mServerDestination, mServerPort);

                    try {

                        //sends the message to the server
                        mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);

                        //receives the message which the server sends back
                        mBufferIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                        //in this while the client listens for the messages sent by the server
                        while (mRun) {

                            mServerMessage = mBufferIn.readLine();

                            if (mServerMessage != null && mMessageListener != null) {
                                //call the method messageReceived from MyActivity class
                                mMessageListener.messageReceived(mServerMessage);
                            }

                        }

                        Log.d(TAG, "S: Received Message: '" + mServerMessage + "'");

                    } catch (Exception e) {
                        Log.e(TAG, "S: Error", e);
                    } finally {
                        //the socket must be closed. It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        mSocket.close();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "C: Error", e);
                }

            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}