import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Producer {
    private KafkaProducer<Integer, Datagram> kafkaProducer;

    private DatagramQueue queue;
    private Reader[] readers;
    private Writer[] writers;

    private static ExecutorService executorService;
    private static DatagramSocket readSocket;

    private Properties config;

    /**
     * Two reader threads read datagrams from a UDP socket: while the other thread
     * is accepting a new datagram, the other is writing the previous packet to a
     * datagram queue. On the other half of the queue there are multiple writer threads
     * emptying Datagram(DatagramPacket, Instant) records from the queue, modifying the
     * data into a suitable event and then sending that event to a Kafka instance.
     *
     * @param defaultConfig Options for the producer process
     */
    public Producer(Properties defaultConfig) {
        this.config = new Properties(defaultConfig);

        try {
            config.setProperty("client.id", InetAddress.getLocalHost().getHostName());
            config.setProperty("bootstrap.servers", "host1:9092");
            config.setProperty("acks", "all");
            kafkaProducer = new KafkaProducer<>(config);
        } catch (Exception e) {
            System.out.println("Error configuring producer: " + e.getMessage());
        }

        int nThreads = Integer.valueOf(config.getProperty("num.readers")) + Integer.valueOf(config.getProperty("num.writers"));
        executorService = Executors.newFixedThreadPool(nThreads);
        this.allocate(
                Integer.valueOf(config.getProperty("udpPort")),
                Integer.valueOf(config.getProperty("bufferSize")),
                Integer.valueOf(config.getProperty("packetSize")),
                Integer.valueOf(config.getProperty("numReaders")),
                Integer.valueOf(config.getProperty("numWriters"))
        );
    }

    public void allocate(int udpPort, int bufferSize, int packetSize, int nReaders, int nWriters) {
        this.queue = new DatagramQueue(bufferSize);
        this.readers = new Reader[nReaders];
        this.writers = new Writer[nWriters];

        try {
            readSocket = new DatagramSocket(udpPort);
        } catch (SocketException e) {
            System.out.println("SocketError: " + e.getMessage());
            Runtime.getRuntime().exit(1);
        }

        for (int i = 0; i < nReaders; i++) {
            this.readers[i] = new Reader(queue, readSocket, packetSize);
        }
        for (int i = 0; i < nWriters; i++) {
            this.writers[i] = new Writer(queue);
        }
    }

    public static void main(String[] args) {

        Producer kafkaProducer = new Producer(initialize(args));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                readSocket.close();
                try {
                    if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                }
                System.out.println("Shutting down...");
            })
        );

        kafkaProducer.start();
    }

    public void start() {
        for (Runnable r : this.readers) {
            executorService.execute(r);
        }
        for (Runnable w : this.writers) {
            executorService.execute(w);
        }
    }

    public static Properties initialize(String[] args) {
        Properties props = new Properties();
        String configName = "default.properties";

        if (args.length == 1) {
            try (InputStream fis = Producer.class.getClassLoader().getResourceAsStream(args[0])) {
                props.loadFromXML(fis);
            } catch (Exception e) {
                System.out.println("Error parsing configuration file: " + e.getMessage());
                System.out.println("Reading default configuration file...");
            }
        } else {
            try (InputStream fis = Producer.class.getClassLoader().getResourceAsStream(configName)) {
                props.loadFromXML(fis);
            } catch (Exception e) {
                System.out.println("Error parsing default configuration file: " + e.getMessage());
                System.out.println("Creating a new default configuration...");

                /* Use default configuration parameters */
                props.setProperty("numReaders", "1");
                props.setProperty("numWriters", "1");
                props.setProperty("packetSize", "1024");
                props.setProperty("bufferSize", "512");
                props.setProperty("udpPort", "65535");
                props.setProperty("kafkaPort", "9092");

                try (FileOutputStream fos = new FileOutputStream(configName)) {
                    props.storeToXML(fos, "");
                } catch (Exception e2) {
                    System.out.println("Error writing configuration file: " + e2.getMessage());
                }
            }
        }

        return props;
    }
}
