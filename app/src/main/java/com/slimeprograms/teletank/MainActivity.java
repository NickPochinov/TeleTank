package com.slimeprograms.teletank;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.slimeprograms.teletank.adapther.BAdapter;
import com.slimeprograms.teletank.adapther.BDevice;
import com.slimeprograms.teletank.adapther.packet.BPacket;
import com.slimeprograms.teletank.tank.TankRequest;
import com.slimeprograms.teletank.tank.TankResponse;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends ComponentActivity {

    BDevice device = null;
    Set<BluetoothDevice> devs = null;

    TankRequest tankRequest = new TankRequest();

    TankResponse response = new TankResponse();

    BluetoothDevice current = null;

    Integer maxClosed = 10;

    Integer closed = 0;

    Boolean showBytes = false;

    Integer interval = 50;

    Boolean isMain = false;


    @SuppressLint("ClickableViewAccessibility")
    public void setupMainActivity() {
        isMain = true;
        setContentView(R.layout.main_activity);
        EditText text = findViewById(R.id.selectDevice);
        ImageButton light = findViewById(R.id.light);
        ImageButton smallGun = findViewById(R.id.smallGun);
        ImageButton bigGun = findViewById(R.id.bigGun);
        ImageButton keyConnect = findViewById(R.id.keyConnect);
        TextView bytes = findViewById(R.id.textView);
        ProgressBar bar = findViewById(R.id.progressBar);
        ConstraintLayout layout = findViewById(R.id.statLayout);
        ComponentActivity activity = this;
        Thread t = new Thread(() -> {
            while (isMain) {
                runOnUiThread(() -> {
                    bytes.setText(tankRequest.toString());
                    keyConnect.setEnabled(device != null);
                    if (device != null) {
                        text.setText(device.getName() + ":" + device.getAddress());
                        if (!response.isEmpty() && response.getChannels().containsKey((byte)13)) {
                            bar.setProgress(response.getChannels().get((byte)13) / 1023 * 100);
                        }
                        layout.setBackgroundColor(response.getStatColor());
                    }
                    else {
                        keyConnect.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.whitekey));
                        keyConnect.setBackground(ContextCompat.getDrawable(activity, R.drawable.circle_button_black));
                        text.setText("Устройство не подключено");
                    }
                });
                try {
                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                }
            }
        });
        t.start();
        bytes.setVisibility(showBytes ? View.VISIBLE : View.INVISIBLE);
        ImageButton search = findViewById(R.id.searchBtn);
        ImageButton setting = findViewById(R.id.settings);
        ImageView left = findViewById(R.id.leftJoystick);
        ImageView right = findViewById(R.id.rightJoystick);

        keyConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tankRequest.getChannels()[4] == 256) {
                    tankRequest.getChannels()[4] = (short) 1023;
                    tankRequest.getChannels()[6] = (short) 1023;
                    light.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.lightactive));
                    keyConnect.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.blackkey));
                    keyConnect.setBackground(ContextCompat.getDrawable(activity, R.drawable.circle_button_white));
                }
                else {
                    keyConnect.setBackground(ContextCompat.getDrawable(activity, R.drawable.circle_button_black));
                    light.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.lightunactive));
                    keyConnect.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.whitekey));
                    tankRequest.getChannels()[4] = (short) 256;
                    tankRequest.getChannels()[6] = (short) 0;
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMain = false;
                setupSearchActivity();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMain = false;
                setupSettingActivity();
            }
        });
        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tankRequest.getChannels()[6] == 0) {
                    light.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.lightactive));
                    tankRequest.getChannels()[6] = (short) 1023;
                }
                else {
                    light.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.lightunactive));
                    tankRequest.getChannels()[6] = (short) 0;
                }
            }
        });
        AtomicBoolean leftPressed = new AtomicBoolean(false);
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    leftPressed.set(true);
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    leftPressed.set(false);
                    tankRequest.clearTankMoveType();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if (leftPressed.get()) {
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();
                        tankRequest.setTankMoveType(x, y, view.getWidth(), view.getHeight());
                    }
                }
                else {
                    return false;
                }
                return true;
            }
        });
        AtomicBoolean rightPressed = new AtomicBoolean(false);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    rightPressed.set(true);
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    rightPressed.set(false);
                    tankRequest.clearGunMoveType();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rightPressed.get()) {
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();
                        tankRequest.setGunMoveType(x, y, view.getWidth(), view.getHeight());
                    }
                }
                else {
                    return false;
                }
                return true;
            }
        });
        bigGun.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tankRequest.getChannels()[5] = 512;
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    tankRequest.getChannels()[5] = 0;
                }
                else {
                    return false;
                }
                return true;
            }
        });
        smallGun.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tankRequest.getChannels()[5] = 256;
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    tankRequest.getChannels()[5] = 0;
                }
                else {
                    return false;
                }
                return true;
            }
        });
    }

    public void setupListDevices(ArrayAdapter<String> adapter) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 0);
            return;
        }
        BluetoothAdapter adapt = BluetoothAdapter.getDefaultAdapter();
        if (adapt != null) {
            adapter.clear();
            devs = BAdapter.findDevices(this);
            if (!devs.isEmpty()) {
                for (BluetoothDevice dev : devs) {
                    adapter.add(dev.getName() + ":" + dev.getAddress());
                }
            }
            else {
                adapter.add("Устройства с Bluetooth не найдены");
            }
        }
    }

    public void setupSearchActivity() {
        setContentView(R.layout.search_activity);
        ImageButton back = findViewById(R.id.home);
        ImageButton search = findViewById(R.id.search);
        Button connect = findViewById(R.id.connect);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView devices = findViewById(R.id.devices);
        if (current != null) {
            if (device == null) {
                current = null;
                connect.setText("Подключить");
                devices.setEnabled(true);
                tankRequest = new TankRequest();
            }
            else {
                try {
                    BluetoothSocket socket = current.createRfcommSocketToServiceRecord(BAdapter.SSP);
                    if (socket != null) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
                        }
                        device = new BDevice(current.getName(), current.getAddress(), BAdapter.SSP);
                        connect.setText("Отключиться");
                        devices.setEnabled(false);
                        connect.setEnabled(true);
                    }
                }
                catch (Exception e) {
                }
            }
        }
        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!devs.isEmpty()) {
                    int index = 0;
                    for (BluetoothDevice dev : devs) {
                        if (index == i) {
                            current = dev;
                            connect.setEnabled(true);
                            break;
                        }
                        index++;
                    }
                }
            }
        });
        ComponentActivity activity = this;
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device != null) {
                    device = null;
                    current = null;
                    connect.setText("Подключить");
                    connect.setEnabled(false);
                    devices.setEnabled(true);
                    tankRequest = new TankRequest();
                }
                else {
                    try {
                        BluetoothSocket socket = current.createRfcommSocketToServiceRecord(BAdapter.SSP);
                        if (socket == null) {
                            return;
                        }
                    }
                    catch (Exception e) {
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
                        return;
                    }
                    device = new BDevice(current.getName(), current.getAddress(), BAdapter.SSP);
                    connect.setText("Отключиться");
                    devices.setEnabled(false);
                }
            }
        });
        devices.setAdapter(adapter);
        setupListDevices(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupMainActivity();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupListDevices(adapter);
            }
        });
    }

    public void setupSettingActivity() {
        setContentView(R.layout.setting_activity);
        ImageButton back = findViewById(R.id.home);
        CheckBox box1 = findViewById(R.id.checkBox);
        EditText intervalText = findViewById(R.id.editTextText);
        box1.setChecked(showBytes);
        intervalText.setText(interval.toString());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBytes = box1.isChecked();
                try {
                    Integer result = Integer.parseInt(intervalText.getText().toString());
                    interval = result > 30 ? result : interval;
                }
                catch (Exception e) {
                }
                setupMainActivity();
            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMainActivity();
        Thread t = new Thread(() -> {
            while (true) {
                if (device != null) {
                    try {
                        tankRequest.send(current, device.getConnectionType(), interval.longValue());
                    }
                    catch (IOException e) {
                    }
                    try {
                        response = new TankResponse(current, device.getConnectionType(), interval.longValue());
                        if (response.isEmpty()) {
                            throw new IOException();
                        }
                        else {
                            response.addToRequest(tankRequest);
                        }
                    }
                    catch (IOException e) {
                        closed++;
                    }
                    if (closed >= maxClosed) {
                        device = null;
                        runOnUiThread(() -> Toast.makeText(this, "Соединение разорвано: Time out", Toast.LENGTH_LONG).show());
                        closed = 0;
                    }
                }
                try {
                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                }
            }
        });
        t.start();
    }
}