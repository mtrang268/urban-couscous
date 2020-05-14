package com.adhoc.proto.entity;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class Record {
    private final RecordType recordType;
    private final long timestamp;
    private final BigInteger userId;
    private final Double dollarAmount;

    private Record(RecordType recordType, long timestamp, BigInteger userId, @Nullable Double dollarAmount) {
        this.recordType = recordType;
        this.timestamp = timestamp;
        this.userId = userId;
        this.dollarAmount = dollarAmount;
    }

    public  RecordType getRecordType() {
        return recordType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BigInteger getUserId() {
        return userId;
    }

    @Nullable
    public Double getDollarAmount() {
        return dollarAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Record record = (Record) o;
        return getTimestamp() == record.getTimestamp() &&
            getRecordType() == record.getRecordType() &&
            Objects.equals(getUserId(), record.getUserId()) &&
            Objects.equals(getDollarAmount(), record.getDollarAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRecordType(), getTimestamp(), getUserId(), getDollarAmount());
    }

    @Override
    public String toString() {
        return "Record{" +
            "recordType=" + recordType +
            ", timestamp=" + timestamp +
            ", userId=" + userId +
            ", dollarAmount=" + dollarAmount +
            '}';
    }

    public static class RecordFactory {
        private static final int NUM_RECORD_BYTES = 20;
        private static final int NUM_AUTO_PAY_RECORD_BYTES = 12;

        static final int NUM_USER_ID_BYTES = 8;

        private final InputStream inputStream;

        public RecordFactory(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        /**
         * Reads the internal input stream to produce a {@link Record}.
         *
         * There are two different encoding patterns for a Record.
         *
         * Start/EndAutopay
         * | 1 byte record type enum | 4 byte (uint32) Unix timestamp | 8 byte (uint64) user ID |
         *
         * Debit/Credit
         * | 1 byte record type enum | 4 byte (uint32) Unix timestamp | 8 byte (uint64) user ID | 8 byte (float64) amount in dollars
         *
         *
         * @return Record generate from the input stream
         * @throws IOException if an error occurs when reading from the internal input stream.
         */
        public Record getRecord() throws IOException {
            final RecordType recordType = RecordType.fromValue((byte) inputStream.read());
            final byte[] recordBytes;
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

            final long timestamp = Integer.toUnsignedLong(byteBuffer.getInt());
            final byte[] userIdBytes = new byte[NUM_USER_ID_BYTES];
            byteBuffer = byteBuffer.get(userIdBytes);
            final BigInteger userId = new BigInteger(userIdBytes);

            final Double dollarAmount = byteBuffer.hasRemaining() ? byteBuffer.getDouble() : null;

            return new Record(recordType, timestamp, userId, dollarAmount);
        }
    }
}
