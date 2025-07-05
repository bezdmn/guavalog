import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Instant;

public class Reader implements Runnable {
    private final DatagramQueue queue;
    private final DatagramSocket socket;
    private final int packetSize;

    public Reader(DatagramQueue queue, DatagramSocket socket, int packetSize) {
        this.queue = queue;
        this.socket = socket;
        this.packetSize = packetSize;
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }
        while (!socket.isClosed()) {
            try {
                byte[] buffer = new byte[packetSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                queue.add(new Datagram(packet, Instant.now()));
            } catch (Exception e) {
                System.out.println("ReadSocketError: " + e.getMessage());
                socket.close();
            }
        }
    }
}
