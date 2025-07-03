import java.net.DatagramPacket;
import java.time.Instant;

/**
 * A datagram encapsulates the UDP packet and the moment it was read from the UDP buffer.
 *
 * @param packet a UDP datagram packet
 * @param time the moment in time when the packet was read from buffer
 */
public record Datagram(DatagramPacket packet, Instant time) {}