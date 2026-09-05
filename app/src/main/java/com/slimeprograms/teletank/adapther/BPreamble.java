package com.slimeprograms.teletank.adapther;

import java.util.Arrays;
import java.util.Objects;

public class BPreamble {
    private byte[] header;

    private String name;

    public BPreamble(String name, byte[] header) {
        this.header = header;
        this.name = name;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BPreamble bPreamble = (BPreamble) o;
        return Arrays.equals(header, bPreamble.header) && name.equals(bPreamble.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(header), name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
