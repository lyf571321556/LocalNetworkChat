package com.digital_mystic.localchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class that holds logic for setting up client-side and server-side connections
 */
public class Connection implements ChatServer.OnClientConnectedListener{
    Handler statusHandler;
    private static final String TAG = "Connection";
    private ChatRemoteClient chatClient;
    private ChatServer chatServer;

    private Socket socket;
    private int port = -1;

    /**
     *
     * @param handler passes messages back to the UI. Use by client and server
     */
    public Connection(Handler handler) {
        statusHandler = handler;
        chatServer = new ChatServer(this);
    }

    public void cleanUp(){
        if(chatClient != null){
            chatClient.shutdown();
        }

        if(chatServer != null){
            chatServer.shutdown();
        }
    }

    public void connectToServer(InetAddress address, int port){
        Log.d("ConnectToServer", "" + address + port);
        chatClient = new ChatRemoteClient(address, port, statusHandler);

    }

    private synchronized void setSocket(Socket socket){
        Log.d(TAG,"setSocket");
        if(socket == null){
            Log.d(TAG, "Socket null");
        }
        //FIXME allows only 1 connection
        if( this.socket !=null){
            if( this.socket.isConnected()){
                try {
                    this.socket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        this.socket = socket;
    }

    private Socket getSocket(){
        return socket;
    }

    public int getPort(){
        return chatServer.getLocalPort();
    }

    private void setPort(int port){
        this.port = port;

    }
    private void updateMessage(String message, boolean isLocal){
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE",message);
        msg.setData(bundle);
        statusHandler.sendMessage(msg);
    }

    public void sendMessage(String msg){
        if(chatClient != null){
            chatClient.sendMessage(msg);
        }
    }

    @Override
    public void onClientConnected(Socket socket) {
        chatClient = new ChatRemoteClient(socket, statusHandler);
    }
}
