package com.guavalog.kafka.producer;

import static org.junit.Assert.*;
import org.junit.*;

public class DatagramBufferTest {
    private DatagramBuffer buf;
    private final int bufferCapacity = 32;
    private final byte[] testByte = new byte[1];

    @Before
    public void create() {
        buf = new DatagramBuffer(bufferCapacity);
    }

    @Test
    public void bufferIsInitializedCorrectly() {
        assertEquals(0, buf.size());
        assertEquals(buf.capacity(), bufferCapacity);
        assertFalse(buf.isFull());
    }

    @Test
    public void addGrowsBufferUpToCapacity() {
        assertEquals(0, buf.size());
        assertTrue(buf.add(testByte));
        assertEquals(1, buf.size());

        for (int i = 0; i < bufferCapacity - 1; i++) {
            assertTrue(buf.add(testByte));
        }

        assertTrue(buf.isFull());
        assertFalse("Adding to a full buffer fails", buf.add(testByte));
    }

    @Test
    public void takeShrinksBufferDownToZero() {
        for (int i = 0; i < 3; i++) {
            assertTrue(buf.add(testByte));
        }

        assertEquals(3, buf.size());
        assertNotEquals(null, buf.take());
        assertNotEquals(null, buf.take());
        assertNotEquals(null, buf.take());

        assertEquals(0, buf.size());
        assertNull("Empty buffer returns null on take", buf.take());
    }

    @Test
    public void bufferCanBeClear() {
        for (int i = 0; i < 3; i++) {
            assertTrue(buf.add(testByte));
        }

        assertEquals(3, buf.size());
        assertEquals(3, buf.clear());
        assertEquals(0, buf.size());

        assertEquals(0, buf.clear());
        assertEquals("Clearing empty buffer stays zero", 0, buf.size());
    }

}
