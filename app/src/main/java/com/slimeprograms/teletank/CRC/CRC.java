package com.slimeprograms.teletank.CRC;

public final class CRC {
    public static byte checksum(byte[] data) {
        byte crc = 0x0;
        for (byte b : data) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc << 1) ^ 0x18);
                }
                else {
                    crc <<= 1;
                }
            }
        }
        return crc;
    }
}
