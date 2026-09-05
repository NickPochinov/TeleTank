package com.slimeprograms.teletank.adapther;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.slimeprograms.teletank.adapther.packet.BPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class BAdapter {
    public static Set<BluetoothDevice> findDevices(Context context) {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return new HashSet<>();
            }
            else {
                return adapter.getBondedDevices();
            }
        }
        catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    public static void sendPacket(BluetoothDevice device, BPacket packet, Long timeout) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(packet.getId());
        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            try {
                socket.connect();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        });
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
            socket.getOutputStream().write(packet.getBytes());
            socket.close();
        }
        catch (TimeoutException e) {
        }
        catch (ExecutionException e) {
        }
        catch (InterruptedException e) {
        }
    }

    public static BPacket getPacket(BluetoothDevice device, UUID connectionType, Long timeout) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(connectionType);
        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            try {
                socket.connect();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        });
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
            byte[] bytes = new byte[]{};
            byte b;
            while ((b = (byte) socket.getInputStream().read()) != 0) {
                bytes = Arrays.copyOf(bytes, bytes.length + 1);
                bytes[bytes.length - 1] = b;
            }
            socket.close();
            return new BPacket(connectionType, bytes);
        }
        catch (TimeoutException e) {
        }
        catch (ExecutionException e) {
        }
        catch (InterruptedException e) {
        }
        return null;
    }

    public static final UUID SSP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
