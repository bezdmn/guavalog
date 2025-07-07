import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Writer implements Runnable {
    private final DatagramQueue queue;
    private KafkaProducer<String, byte[]> producer;
    private int packetSize;

    private volatile boolean running;
    /* IPv4 + UDP header size */
    private static int headerSize = 28;

    public Writer(DatagramQueue queue, KafkaProducer<String, byte[]> producer, int packetSize) {
        this.queue = queue;
        this.producer = producer;
        this.packetSize = packetSize;
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        byte[] sourceAddrBytes = new byte[4];
        byte[] payload = new byte[packetSize - headerSize];
        while (running) {
            try {
                Thread.sleep(1000);
                ByteBuffer packet = ByteBuffer.wrap(queue.take());
                short sourcePort = packet.getShort(20);
                for (int i = 0; i < 4; i++) { sourceAddrBytes[0] = packet.get(12 + i); }
                // TODO: manage IP header options;
                String sourceAddrStr = InetAddress.getByAddress(sourceAddrBytes).toString() + ":" + sourcePort;
                packet.get(payload, headerSize, packetSize - headerSize);
                producer.send(new ProducerRecord<String, byte[]>(
                        "test-topic", sourceAddrStr, payload));
            } catch (Exception e) {
                System.out.println("WriterError: " + e.getMessage());
            }
        }
    }
}
