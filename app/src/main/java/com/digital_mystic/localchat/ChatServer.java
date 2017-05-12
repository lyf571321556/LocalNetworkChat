package com.digital_mystic.localchat;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jonathan on 5/9/2017.
 */

public class ChatServer {
    private static final String TAG = "ChatServer";
    private Thread acceptThread;
    private ServerSocket mServerSocket;
    private OnClientConnectedListener clientConnectedListener;

    private int localPort;
    private static boolean isServer;
        
    public static boolean isServer(){
        return isServer;
    }

    int getLocalPort() {
        return localPort;
    }

    void setLocalPort(int localPort){
        this.localPort = localPort;
    }

    ChatServer(OnClientConnectedListener connectedListener){
        clientConnectedListener = connectedListener;
        try {
            mServerSocket = new ServerSocket(0);
            setLocalPort(mServerSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        acceptThread = new Thread(new ServerThread());
        acceptThread.start();
    }

    void shutdown(){
        if(!mServerSocket.isClosed()){
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ServerThread implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "Server listening:");
                    Socket socket = mServerSocket.accept();
                    Log.d(TAG, "Client accepted.");
                    Log.d(TAG, "Client: " + socket.getInetAddress()+ ":"+ socket.getPort());
                    clientConnectedListener.onClientConnected(socket);
                    isServer = true;
                }
            } catch (IOException e){
                Log.d("Server", "Server socket error", e);
                e.printStackTrace();

            }
        }
    }

    interface OnClientConnectedListener {
        void onClientConnected(Socket socket);
    }
}
