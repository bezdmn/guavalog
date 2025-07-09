import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Properties;

public class Reader implements Runnable {
    private final DatagramQueue queue;
    private DatagramSocket socket;
    private final int packetSize;
    private static int numReaders = 0;

    public Reader(DatagramQueue queue, Properties config) {
        this.queue = queue;
        this.packetSize = Integer.parseInt(config.getProperty("packetSize"));

        try (DatagramSocket socket = new DatagramSocket(Integer.parseInt(config.getProperty("udpPort")) + numReaders)) {
            this.socket = socket;
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
            /* Reader thread is created but non-functioning */
        }

        numReaders++;
    }

    public void stop() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }

        byte[] buffer = new byte[packetSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (!socket.isClosed()) {
            try {
                socket.receive(packet);
                queue.add(buffer.clone());
            } catch (SocketException e) {
                System.out.println("SocketException: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Reader error: " + e.getMessage());
                socket.close();
            }
        }
    }
}
