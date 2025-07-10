package com.guavalog.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Properties;

public class Writer implements Runnable {
    private final DatagramQueue queue;
    private KafkaProducer<String, byte[]> producer;
    private int packetSize;
    private volatile boolean running;
    private final int headerSize = 28; // IPv4 + UDP header size
    private static int nWriters = 0;

    public Writer(DatagramQueue queue, Properties config) {
        this.queue = queue;
        this.running = true;

        nWriters++;

        if (nWriters == 1) {
            this.init(config);
        }
    }

    private void init(Properties config) {
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "TestProducer");
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put("acks", "all");
        config.put("retries", "0");

        try {
            producer = new KafkaProducer<>(config);
        } catch (KafkaException e) {
            System.out.println("Error configuring producer: " + e.getMessage());
            Runtime.getRuntime().exit(1);
        }

        this.packetSize = Integer.parseInt(config.getProperty("packetSize"));
    }

    public void stop() {
        this.running = false;
        producer.close();
    }

    @Override
    public void run() {
        byte[] sourceAddrBytes = new byte[4];
        byte[] payload = new byte[packetSize - headerSize];
        while (running) {
            try {
                ByteBuffer packet = ByteBuffer.wrap(queue.take());
                short sourcePort = packet.getShort(20);
                for (int i = 0; i < 4; i++) { sourceAddrBytes[0] = packet.get(12 + i); }
                // TODO: manage IP header options;
                String sourceAddrStr = InetAddress.getByAddress(sourceAddrBytes).toString() + ":" + sourcePort;
                packet.get(payload, headerSize, packetSize - headerSize);
                producer.send(new ProducerRecord<String, byte[]>(
                        "test-topic", sourceAddrStr, payload));
            } catch (InterruptedException e) {
                System.out.println("com.guavalog.kafka.producer.Writer interrupted");
            } catch (Exception e) {
                System.out.println("WriterError: " + e.getMessage());
            }
        }
    }
}
