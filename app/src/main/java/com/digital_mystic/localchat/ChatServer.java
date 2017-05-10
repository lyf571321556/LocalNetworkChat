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
    Thread acceptThread;
    ServerSocket mServerSocket;
    Socket socket;
    OnClientConnectedListener clientConnectedListener;

    private int localPort;

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort){
        this.localPort = localPort;
    }

    public Socket getClientSocket(){
        return socket;
    }

    private void setClientSocket(Socket socket){
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (this.socket != null) {
            if (this.socket.isConnected()) {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        this.socket = socket;
    }


    public ChatServer(OnClientConnectedListener connectedListener){
        clientConnectedListener = connectedListener;
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        acceptThread = new Thread(new ServerThread());
        acceptThread.start();
    }



    public void shutdown(){
        if(!mServerSocket.isClosed()){
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class ServerThread implements Runnable {

        @Override
        public void run() {
            try {
                setLocalPort(mServerSocket.getLocalPort());
                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "Server listening:");
                    setClientSocket(mServerSocket.accept());
                    Log.d(TAG, "Client accepted.");
                    Log.d(TAG, "Client: " + getClientSocket().getInetAddress()+ ":"+ getClientSocket().getPort());
                    clientConnectedListener.onClientConnected(getClientSocket());
                }
            } catch (IOException e){
                Log.d("Server", "Error creating serversocket", e);
                e.printStackTrace();

            }
        }
    }

    interface OnClientConnectedListener {
        void onClientConnected(Socket socket);
    }
}
