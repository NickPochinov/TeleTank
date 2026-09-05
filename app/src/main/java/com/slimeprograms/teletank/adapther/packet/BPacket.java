package com.slimeprograms.teletank.adapther.packet;

import java.util.UUID;

public class BPacket {
    private UUID id;
    private byte[] bytes;

    public BPacket(UUID id, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
