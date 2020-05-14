package com.adhoc.entity;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class RecordFactory {
    private static final int NUM_RECORD_BYTES = 20;
    private static final int NUM_AUTO_PAY_RECORD_BYTES = 12;

    static final int NUM_USER_ID_BYTES = 8;

    private final InputStream inputStream;

    public RecordFactory(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Record getRecord() throws IOException {
        RecordType recordType = RecordType.fromValue((byte) inputStream.read());
        byte[] recordBytes;
        switch (recordType) {
            case Debit:
            case Credit:
                recordBytes = new byte[NUM_RECORD_BYTES];
                break;
            case StartAutopay:
            case EndAutopay:
                recordBytes = new byte[NUM_AUTO_PAY_RECORD_BYTES];
                break;
            default:
                throw new IllegalStateException(String.format("RecordType %s not supported", recordType.name()));
        }

        Preconditions.checkState(inputStream.read(recordBytes) == recordBytes.length);

        ByteBuffer byteBuffer = ByteBuffer.wrap(recordBytes);

        long timestamp = Integer.toUnsignedLong(byteBuffer.getInt());
        byte[] userIdBytes = new byte[NUM_USER_ID_BYTES];
        byteBuffer = byteBuffer.get(userIdBytes);
        BigInteger userId = new BigInteger(userIdBytes);

        Double dollarAmount = byteBuffer.hasRemaining() ? byteBuffer.getDouble() : null;

        return new Record(recordType, timestamp, userId, dollarAmount);
    }
}
