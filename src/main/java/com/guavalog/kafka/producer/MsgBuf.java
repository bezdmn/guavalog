package com.guavalog.kafka.producer;

import java.lang.foreign.*;
import java.util.stream.Stream;

public class MsgBuf {
    private final MemorySegment segment;

    static final int DATA_SIZE = 1012;

    static final SequenceLayout MSG_LAYOUT
            = MemoryLayout.sequenceLayout(64,
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("ip"),
                        ValueLayout.JAVA_INT.withName("port"),
                        ValueLayout.JAVA_INT.withName("time"),
                        MemoryLayout.sequenceLayout(DATA_SIZE, ValueLayout.JAVA_BYTE).withName("payload")
            )
    );

    public MsgBuf() throws RuntimeException {
        try (Arena arena = Arena.ofShared()) {
            this.segment = arena.allocate(MSG_LAYOUT, 4);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int size() {
        assert MSG_LAYOUT.elementCount() <= Integer.MAX_VALUE;
        return (int) MSG_LAYOUT.elementCount();
    }

    public Stream<MemorySegment> stream() {
        return segment.asReadOnly().elements(MSG_LAYOUT);
    }
}
