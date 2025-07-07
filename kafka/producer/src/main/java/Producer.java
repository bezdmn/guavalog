import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer {
    private DatagramQueue queue;
    private Reader[] readers;
    private Writer[] writers;

    private static KafkaProducer<String, byte[]> producer;
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
        config = new Properties(defaultConfig);
        config.put("acks", "all");
        config.put("retries", "0");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, "TestProducer");
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try {
            producer = new KafkaProducer<>(config);
        } catch (Exception e) {
            System.out.println("Error configuring producer: " + e.getMessage());
            Runtime.getRuntime().exit(1);
        }

        int nThreads = Integer.parseInt(config.getProperty("numReaders")) + Integer.parseInt(config.getProperty("numWriters"));
        executorService = Executors.newFixedThreadPool(nThreads);
        this.allocate(
                Integer.parseInt(config.getProperty("udpPort")),
                Integer.parseInt(config.getProperty("bufferSize")),
                Integer.parseInt(config.getProperty("packetSize")),
                Integer.parseInt(config.getProperty("numReaders")),
                Integer.parseInt(config.getProperty("numWriters"))
        );
    }

    public void allocate(int udpPort, int bufferSize, int packetSize, int nReaders, int nWriters) {
        this.queue = new DatagramQueue(bufferSize, nWriters);
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
            this.writers[i] = new Writer(queue, producer, packetSize);
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
                producer.close();
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
        String defaultConf = "default.properties";
        String path = Producer.class.getClassLoader().getResource("").getPath();

        if (args.length == 1) {
            try (InputStream fis = new FileInputStream(path + args[0])) {
                props.loadFromXML(fis);
                return props;
            } catch (Exception e) {
                System.out.println("Error parsing configuration file: " + e.getMessage());
                System.out.println("Reading default configuration file...");
            }
        }

        try (InputStream fis = new FileInputStream(path + defaultConf)) {
            props.loadFromXML(fis);
            return props;
        } catch (Exception e) {
            System.out.println("Error parsing default configuration file: " + e.getMessage());
            System.out.println("Creating a new default configuration...");
        }

        /* Set the default configuration parameters and write them to file */

        props.setProperty("numReaders", "1");
        props.setProperty("numWriters", "1");
        props.setProperty("packetSize", "1024");
        props.setProperty("bufferSize", "512");
        props.setProperty("udpPort", "65535");
        props.setProperty("kafkaPort", "9092");

        try (FileOutputStream fos = new FileOutputStream(path + defaultConf)) {
            props.storeToXML(fos, "");
        } catch (Exception e) {
            System.out.println("Error writing configuration file: " + e.getMessage());
        }

        return props;
    }
}
