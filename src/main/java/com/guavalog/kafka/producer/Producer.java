package com.guavalog.kafka.producer;

import java.lang.foreign.*;

public class Producer {
    private MemorySegment segmentA;
    private MemorySegment segmentB;

    static final int mtuSize = 1500;

    static final SequenceLayout udpLayout
            = MemoryLayout.sequenceLayout(10,
                MemoryLayout.structLayout(
                        ValueLayout.JAVA_INT.withName("IntTest"),
                        MemoryLayout.sequenceLayout(mtuSize, ValueLayout.JAVA_BYTE).withName("ByteTest")
                )
            );

    public static void main(String[] args) {
        Producer kafkaProducer = new Producer();

        try {
            kafkaProducer.allocate();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void allocate() throws RuntimeException {

        try (Arena arena = Arena.ofShared()) {

            this.segmentA = arena.allocate(udpLayout);
            this.segmentB = arena.allocate(udpLayout);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public MemorySegment getSegment(char ab) {
        if (ab == 'a') return this.segmentA;
        else if (ab == 'b') return this.segmentB;
        return null;
    }
}
