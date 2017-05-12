package com.digital_mystic.localchat;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    boolean isNetworkeAccessGranted;
    NsdHelper nsdHelper;
    Connection connection;
    static Handler incomingHandler;
    EditText messageHistoryET;
    EditText messageET;

    boolean isServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        incomingHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Bundle messageBundle = msg.getData();
                String message = messageBundle.getString("MESSAGE");
                messageHistoryET.append("\n" + message);
            }
        };

        messageHistoryET = (EditText) findViewById(R.id.messageHistoryET);
        messageET = (EditText) findViewById(R.id.sendMessageET);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.INTERNET},10);
            } else {
                isNetworkeAccessGranted = true;
                init();
            }
        } else {
            isNetworkeAccessGranted = true;
            init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connection.cleanUp();
        nsdHelper.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.connection_options, menu);
        return true;
    }
    private void init(){
        nsdHelper = new NsdHelper(this);
        connection = new Connection(incomingHandler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isNetworkeAccessGranted = true;
                init();
            } else {
                isNetworkeAccessGranted = false;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.connect:
                Toast.makeText(this, R.string.connecting_toast, Toast.LENGTH_LONG).show();
                if(nsdHelper.getSelectedService() != null){
                    NsdServiceInfo serviceInfo = nsdHelper.getSelectedService();
                    connection.connectToServer(serviceInfo.getHost(), serviceInfo.getPort());
                }
                isServer = false;
                break;
            case R.id.discover:
                Toast.makeText(this, R.string.discovering_toast, Toast.LENGTH_LONG).show();
                nsdHelper.discoverServices();
                break;
            case R.id.register:
                Toast.makeText(this, R.string.register_toast, Toast.LENGTH_LONG).show();
                connection.startServer();
                isServer = true;
                nsdHelper.registerService(connection.getPort());
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(connection != null){
            connection.sendMessage(messageET.getText().toString());
            messageET.setText("");
        }
    }
}
