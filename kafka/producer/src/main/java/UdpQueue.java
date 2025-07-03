import java.lang.foreign.MemorySegment;
import java.util.stream.Stream;

/**
 * A double buffered ("ping-pong buffer") blocking queue for transient storage
 * of UDP packets. The queue is backed by two buffers implemented with
 * MemorySegments; these segments have a sequence layout, where each element
 * matches the structure of a datagram packet as defined in UdpBuffer.
 */

public class UdpQueue {

    private UdpBuffer bufA;
    private UdpBuffer bufB;

    public UdpQueue() {
        try {
            this.bufA = new UdpBuffer();
            this.bufB = new UdpBuffer();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Stream<MemorySegment> take() {
        while (true) {
            if (bufA.isFull()) {
                return bufA.stream();
            } else if (bufB.isFull()) {
                return bufB.stream();
            }
        }
    }

    public UdpBuffer put() {
        while (true) {
            if (bufA.isFull()) {
                return bufB;
            } else if (bufB.isFull()) {
                return bufA;
            }
        }
    }
}
