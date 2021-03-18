package com.example.hexagonlights;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GridAdapter.OnNoteListener{
    public final static String MODULE_MAC = "00:20:10:08:8A:84";
    //public final static String MODULE_MAC = "00:20:10:08:90:9B";
    public final static int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private byte function;

    BluetoothAdapter bta;
    BluetoothSocket mmSocket;
    BluetoothDevice hc06;
    SeekBar sRed, sBlue, sGreen, sbrightness;
    Button btn_submit;
    ImageButton btn_connect;

    RecyclerView hexaRcv;
    GridLayoutManager manager;
    GridAdapter adapter;
    OutputStream outputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_submit = findViewById(R.id.submit);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.functions, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Position", String.valueOf(i));
                function = (byte) (i+7);
                System.out.println(function);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sRed = findViewById(R.id.seekBar_red);
        sGreen = findViewById(R.id.seekBar_green);
        sBlue = findViewById(R.id.seekBar_blue);
        sbrightness = findViewById(R.id.seekbar_brightness);
        btn_connect = findViewById(R.id.reconnect);
        hexaRcv = findViewById(R.id.hexa_rcv);
        adapter = new GridAdapter(this, this);
        manager = new GridLayoutManager(this, 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //6 hex
                int size = 1;
                if((position+1) % 3 == 0) {
                    size = 1;
                }
                return size;
                //3 hex
//                int size = 2;
//                if((position+1)%3 == 0) {
//                    size = 1;
//                }
//                return size;
            }
        });
        hexaRcv.setLayoutManager(manager);
        hexaRcv.setAdapter(adapter);
        bta = BluetoothAdapter.getDefaultAdapter();
        hc06 = bta.getRemoteDevice(MODULE_MAC);
        System.out.println(hc06.getName());
        mmSocket = null;
        int counter = 0;
        do {
            try {
                mmSocket = hc06.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                System.out.println(mmSocket);
                mmSocket.connect();
                System.out.println(mmSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while(!mmSocket.isConnected() && counter < 5);

        try {
            outputStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            InputStream is = mmSocket.getInputStream();
//            is.skip(is.available());
//            byte b = (byte) is.read();
//            System.out.println((char)b);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        btn_submit.setOnClickListener(view -> {
            int r,g,b;
            int brightness = sbrightness.getProgress();

            if(function == 13) {
                r= (int) (((sRed.getProgress()*255)/100)*brightness);
                g= (int) (((sGreen.getProgress()*255)/100)*brightness);
                b= (int) (((sBlue.getProgress()*255)/100)*brightness);
                byte[] msg = {(byte) r, (byte) g, (byte) b, function, '\n'};
                try {
                    outputStream.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                byte[] msg = {(byte) brightness, (byte) brightness, (byte) brightness, function};
                try {
                    outputStream.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_connect.setOnClickListener(view -> {
            int counter1 = 0;
            do {
                try {
                    mmSocket = hc06.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    System.out.println(mmSocket);
                    mmSocket.connect();
                    System.out.println(mmSocket.isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter1++;
            } while(!mmSocket.isConnected() && counter1 < 5);
            try {
                outputStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onNoteClick(int position){
        int r,g,b;
        if(position == 0) {
            position = 4;
        }
        if(position == 1) {
            position = 3;
        }
        if(position == 2) {
            position = 1;
        }
        if(position == 3) {
            position = 5;
        }
        if(position == 4) {
            position = 0;
        }
        if(position == 5) {
            position = 2;
        }
        double brightness = sbrightness.getProgress() / 100.0;
        r= (int) (((sRed.getProgress()*255)/100)*brightness);
        g= (int) (((sGreen.getProgress()*255)/100)*brightness);
        b= (int) (((sBlue.getProgress()*255)/100)*brightness);
        byte[] msg = {(byte) r, (byte) g, (byte) b, (byte) position};
        try {
            outputStream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapter.setColor(r, g, b);
        Log.d("Button", "" + position);
    }
}