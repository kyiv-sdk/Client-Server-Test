package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.DataOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket s = new Socket("10.0.2.2",6666);
                    DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                    for (int i = 0; i < 4; i++){
                        dout.writeUTF("Hello Server");
                        //Thread.sleep(3400);
                        dout.flush();
                        //s.close();
                    }
                    dout.writeUTF("quit");
                    dout.flush();
                    dout.close();
                    s.close();
                }catch(Exception e){
                    Log.d("Log",  "Caught exception");
                    System.out.println(e);
                }
            }
        }).start();
    }
}
