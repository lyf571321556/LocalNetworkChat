package com.digital_mystic.localchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds logic for setting up client-side and server-side connections
 */
public class Connection implements ChatServer.OnClientConnectedListener, ChatClient.OnMessageReceivedListener{
    private Handler statusHandler;
    private static final String TAG = "Connection";
    private ChatClient chatClient;
    private ChatServer chatServer;
    private List<ChatClient> clientList;

    /**
     *
     * @param handler passes messages back to the UI. Used by client and server
     */
    Connection(Handler handler) {
        statusHandler = handler;
        clientList = new ArrayList<>();
    }

    void startServer(){
        chatServer = new ChatServer(this);
    }

    void cleanUp(){
        if(chatClient != null){
            chatClient.shutdown();
        }

        if(chatServer != null){
            chatServer.shutdown();
        }
    }

    void connectToServer(InetAddress address, int port){
        Log.d("ConnectToServer", "" + address + port);
        chatClient = new ChatClient(address, port, statusHandler);
        chatClient.setOnMessageReceivedListener(this);
        clientList.add(chatClient);

    }

    int getPort(){
        return chatServer.getLocalPort();
    }

    void sendMessage(String msg){
        //Prepend username and send message to clients
        StringBuilder builder = new StringBuilder();
        builder.append(ChatClient.defaultUserName);
        builder.append(":");
        builder.append(msg);
        forwardMessage(builder.toString());

        //Pass message back to UI
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE",builder.toString());
        bundle.putBoolean("LOCAL",true);
        message.setData(bundle);
        statusHandler.sendMessage(message);
    }

    void forwardMessage(String msg){
        for(ChatClient c: clientList){
            if(c != null){
                c.sendMessage(msg);
            }
        }

    }

    @Override
    public void onClientConnected(Socket socket) {
        chatClient = new ChatClient(socket, statusHandler);
        chatClient.setOnMessageReceivedListener(this);
        clientList.add(chatClient);
    }

    @Override
    public void onMessageReceived(String message) {
        if(ChatServer.isServer()){
            forwardMessage(message);
        }
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE", message);
        msg.setData(bundle);
        statusHandler.sendMessage(msg);

    }
}
