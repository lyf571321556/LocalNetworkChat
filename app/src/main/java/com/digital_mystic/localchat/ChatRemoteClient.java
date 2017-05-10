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
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Jonathan on 5/10/2017.
 */


public class ChatRemoteClient {
    private static final String TAG = "CLIENT";
    private InetAddress serverAddress;
    private int serverPort;
    private Thread sendThread;
    private Thread recThread;
    private Socket socket;

    private Handler uiHandler;

    public ChatRemoteClient(InetAddress serverAddress, int serverPort, Handler handler){
        Log.d(TAG, "ServerAddress: " + serverAddress);
        Log.d(TAG, "ServerPort: " + serverPort);

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        uiHandler = handler;
        sendThread = new Thread(new SendThread());
        sendThread.start();

    }

    public ChatRemoteClient(Socket socket, Handler handler){
        Log.d(TAG, "ServerAddress: " + serverAddress);
        Log.d(TAG, "ServerPort: " + serverPort);

        this.serverAddress = socket.getInetAddress();
        this.serverPort = socket.getPort();
        setSocket(socket);
        uiHandler = handler;
        sendThread = new Thread(new SendThread());
        sendThread.start();

    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void shutdown(){
        if(!socket.isClosed()){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void passReceivedMessage(String string){
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("MESSAGE", string);
        msg.setData(bundle);
        uiHandler.sendMessage(msg);
    }


    class ReceiveThread implements Runnable{

        @Override
        public void run() {
            BufferedReader input;
            try {
                input = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                while (!Thread.currentThread().isInterrupted()){
                    String messageStr = null;
                    messageStr = input.readLine();
                    if (messageStr != null) {
                        Log.d(TAG, "Read from the stream: " + messageStr);
                        passReceivedMessage(messageStr);
                    } else {
                        Log.d(TAG, "Null message");
                        break;
                    }
                }
                input.close();
            } catch (IOException e){
                Log.e(TAG, "Server loop error: ", e);
            }

        }
    }

    class SendThread implements Runnable{
        @Override
        public void run() {
            try {
                if (getSocket() == null) {
                    Log.d(TAG, "Client socket");
                    setSocket(new Socket(serverAddress, serverPort));
                }
                recThread = new Thread(new ReceiveThread());
                recThread.start();
            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown host", e);
            } catch (IOException e) {
                Log.d(TAG, "IO error", e);
            }
        }
    }

    public void sendMessage(String msg){
        try {
            Socket socket = getSocket();
            if (socket == null) {
                Log.d(TAG, "Socket null!");
            } else if (socket.getOutputStream() == null) {
                Log.d(TAG, "Socket output stream null!");
            }
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(getSocket().getOutputStream())), true);
            out.println(msg);
            out.flush();
            passReceivedMessage(msg);
        } catch (UnknownHostException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




}
