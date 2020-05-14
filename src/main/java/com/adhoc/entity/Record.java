package com.adhoc.entity;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

public class Record {
    private final RecordType recordType;
    private final long timestamp;
    private final BigInteger userId;
    private final Double dollarAmount;

    Record(RecordType recordType, long timestamp, BigInteger userId, @Nullable Double dollarAmount) {
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
}
