package com.slimeprograms.teletank.tank;

import android.bluetooth.BluetoothDevice;

import com.slimeprograms.teletank.CRC.CRC;
import com.slimeprograms.teletank.adapther.BAdapter;
import com.slimeprograms.teletank.adapther.BPreamble;
import com.slimeprograms.teletank.adapther.packet.BPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class TankRequest implements Cloneable {
    public static final Integer CHANNELS_SIZE = 7;
    private final Short[] channels = new Short[CHANNELS_SIZE];

    public static final BPreamble TANK1 = new BPreamble("TANK1", new byte[]{0x22, 0x43});
    public static final BPreamble TANK2 = new BPreamble("TANK2", new byte[]{0x22, 0x44});
    public TankRequest() {
        Arrays.fill(channels, (short) 0);
        channels[4] = 256;
    }
    public void send(BluetoothDevice device, UUID connectionType, Long timeout) throws IOException {
        BAdapter.sendPacket(device, new BPacket(connectionType, getBytes()), timeout);
    }

    public void clearChannels() {
        Arrays.fill(channels, (short) 0);
        channels[4] = 256;
    }

    public byte[] getBaseBytes() {
        byte[] buff = new byte[TANK1.getHeader().length + CHANNELS_SIZE * 2];
        System.arraycopy(TANK1.getHeader(), 0, buff, 0, TANK1.getHeader().length);
        ByteBuffer buffer = ByteBuffer.wrap(buff);
        for (Short channel : channels) {
            if (TANK1.equals(TANK2)) {

            }
            buffer.putShort(channel);
        }
        return buffer.array();
    }

    public byte[] getBytes() {
        byte[] base = getBaseBytes();
        byte[] buff = new byte[base.length + 1];
        System.arraycopy(base, 0, buff, 0, base.length);
        ByteBuffer buffer = ByteBuffer.wrap(buff);
        buffer.put(getCRC());
        return buffer.array();
    }

    public byte getCRC() {
        return CRC.checksum(getBaseBytes());
    }

    public Short[] getChannels() {
        return channels;
    }

    @Override
    public String toString() {
        return "Tank{" +
                "channels=" + Arrays.toString(channels) +
                '}';
    }

    @Override
    public TankRequest clone() {
        try {
            TankRequest clone = (TankRequest) super.clone();
            return clone;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void setTankMoveType(float x, float y, float width, float height) {
        float diffx = width / 1023;
        float diffy = height / 1023;
        channels[3] = (short) Math.min(Math.max(x / diffx, 0), 1023);
        channels[2] = (short) Math.min(Math.max(1023 - y / diffy, 0), 1023);
    }

    public void setGunMoveType(float x, float y, float width, float height) {
        float diffx = width / 1023;
        float diffy = height / 1023;
        channels[1] = (short) Math.min(Math.max(x / diffx, 0), 1023);
        channels[0] = (short) Math.min(Math.max(1023 - y / diffy, 0), 1023);
    }

    public void clearTankMoveType() {
        channels[3] = 0;
        channels[2] = 0;
    }

    public void clearGunMoveType() {
        channels[1] = 0;
        channels[0] = 0;
    }
}
