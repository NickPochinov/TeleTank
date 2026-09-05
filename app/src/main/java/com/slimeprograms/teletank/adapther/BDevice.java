package com.slimeprograms.teletank.adapther;

import java.util.UUID;

public class BDevice {

    private final String name;
    private final String address;
    private final UUID connectionType;

    public BDevice(String name, String address, UUID connectionType) {
        this.name = name;
        this.address = address;
        this.connectionType = connectionType;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
    public UUID getConnectionType() {
        return connectionType;
    }
}
