package com.slimeprograms.teletank.tank;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;

import com.slimeprograms.teletank.CRC.CRC;
import com.slimeprograms.teletank.adapther.BAdapter;
import com.slimeprograms.teletank.adapther.packet.BPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TankResponse {
    private final Map<Byte, Short> channels = new HashMap<>();
    private boolean empty = true;

    public TankResponse(BluetoothDevice device, UUID connectionType, Long timeout) throws IOException {
        BPacket packet = BAdapter.getPacket(device, connectionType, timeout);
        if (packet == null || packet.getBytes().length == 0) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(packet.getBytes(), TankRequest.TANK2.getHeader().length, packet.getBytes().length - TankRequest.TANK2.getHeader().length - 1);
        byte[] byteChannels = buffer.array();
        if (packet.getBytes()[packet.getBytes().length - 1] != CRC.checksum(byteChannels)) {
            return;
        }
        for (int i = 0; i < byteChannels.length; i += 3) {
            byte key = byteChannels[i];
            short value = ByteBuffer.wrap(new byte[]{byteChannels[i + 1], byteChannels[i + 2]}).getShort();
            channels.put(key, value);
        }
        empty = false;
    }

    public TankResponse() {
    }

    public Map<Byte, Short> getChannels() {
        return channels;
    }

    public void addToRequest(TankRequest tankRequest) {
        if (empty) {
            return;
        }
        for (Byte key : channels.keySet()) {
            if (key > 0 && key <= TankRequest.CHANNELS_SIZE) {
                tankRequest.getChannels()[key - 1] = channels.get(key);
            }
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public int getStatColor() {
        if (empty || !channels.containsKey((byte)14)) {
            return Color.GRAY;
        }
        short colorBytes = channels.get((byte)14);
        int col = colorBytes & 0xFF;
        int r3 = (col >> 5) & 0x07;
        int g3 = (col >> 2) & 0x07;
        int b2 = col & 0x03;
        int red = (r3 * 255) / 7;
        int green = (g3 * 255) / 7;
        int blue = (b2 * 255) / 3;
        return Color.rgb(red, green, blue);
    }
}
