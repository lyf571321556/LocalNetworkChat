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
public class Connection {
    Handler statusHandler;
    private static final String TAG = "Connection";
    private ChatClient chatClient;
    private ChatServer chatServer;

    private Socket socket;
    private int port = -1;

    /**
     *
     * @param handler passes messages back to the UI. Use by client and server
     */
    public Connection(Handler handler){
        statusHandler = handler;
        chatServer = new ChatServer();
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
        chatClient = new ChatClient(address, port);

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
        return port;
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


    public class ChatServer {
        Thread acceptThread;
        ServerSocket mServerSocket;

        public ChatServer(){
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
                    mServerSocket = new ServerSocket(0);
                    setPort(mServerSocket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "Server listening:");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Client accepted.");
                        Log.d(TAG, "Client: " + getSocket().getInetAddress()+ ":"+ getSocket().getPort());
                        if(chatClient == null){
                            chatClient = new ChatClient(getSocket().getInetAddress(), getSocket().getPort());
                        }
                    }
                } catch (IOException e){
                    Log.d("Server", "Error creating serversocket", e);
                    e.printStackTrace();

                }
            }
        }
    }

    public class ChatClient {
        private static final String CLIENT_TAG = "CLIENT";
        private InetAddress serverAddress;
        private int serverPort;
        private Thread sendThread;
        private Thread recThread;

        public ChatClient(InetAddress serverAddress, int serverPort){
            Log.d(TAG, "ServerAddress: " + serverAddress);
            Log.d(TAG, "ServerPort: " + serverPort);

            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            sendThread = new Thread(new SendThread());
            sendThread.start();

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
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
                            updateMessage(messageStr, false);
                        } else {
                            Log.d(CLIENT_TAG, "Null message");
                            break;
                        }
                    }
                    input.close();
                } catch (IOException e){
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
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
//                while (true){
//                  try {
//            } catch (InterruptedException ie)
//      String msg = mMessageQueue.take();
//                    sendMessage(msg);

//}
//                }
            }
        }

        public void sendMessage(String msg){
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket null!");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream null!");
                }
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessage(msg, true);
            } catch (UnknownHostException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }




    }
}
