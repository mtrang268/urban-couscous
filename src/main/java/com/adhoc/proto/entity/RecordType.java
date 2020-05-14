package com.adhoc.proto.entity;

import com.google.common.base.Preconditions;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RecordType {
    Debit((byte) 0x00),
    Credit((byte) 0x01),
    StartAutopay((byte) 0x02),
    EndAutopay((byte) 0x03);

    public static final Map<Byte, RecordType> BYTE_RECORD_TYPE_MAP = EnumSet.allOf(RecordType.class)
        .stream()
        .collect(Collectors.toMap(RecordType::getValue, Function.identity()));

    private final byte value;

    RecordType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static RecordType fromValue(byte value) {
        RecordType result = BYTE_RECORD_TYPE_MAP.get(value);
        Preconditions.checkNotNull(result, String.format("No record type found for value %d", value));
        return result;
    }
}
