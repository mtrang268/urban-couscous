package com.adhoc.entity;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Header {
    private static final int NUM_MAGIC_STRING_BYTES = 4;
    private final String magicString;
    private final byte version;
    private final long numRecords;

    private Header(String magicString, byte version, long numRecords) {
        this.magicString = magicString;
        this.version = version;
        this.numRecords = numRecords;
    }

    public String getMagicString() {
        return magicString;
    }

    public byte getVersion() {
        return version;
    }

    public long getNumRecords() {
        return numRecords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Header header = (Header) o;
        return getVersion() == header.getVersion() &&
            getNumRecords() == header.getNumRecords() &&
            Objects.equals(getMagicString(), header.getMagicString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMagicString(), getVersion(), getNumRecords());
    }

    @Override
    public String toString() {
        return "Header{" +
            "magicString='" + magicString + '\'' +
            ", version=" + version +
            ", numRecords=" + numRecords +
            '}';
    }

    /**
     * Header should always contain {@link Constants#NUM_HEADER_BYTES} bytes.
     * Format: | 4 byte magic string | 1 byte version | 4 byte (uint32) # of records |
     *
     * @param input Header encoded as a byte array
     * @return Header generated from the input byte array
     */
    public static Header parse(byte[] input) {
        Preconditions.checkState(input.length == Constants.NUM_HEADER_BYTES);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(input);

        final byte[] magicStringBytes = new byte[NUM_MAGIC_STRING_BYTES];
        byteBuffer.get(magicStringBytes);
        final String magicString = new String(magicStringBytes);

        final byte version = byteBuffer.get();
        final long numRecords = Integer.toUnsignedLong(byteBuffer.getInt());

        return new Header(magicString, version, numRecords);
    }
}
