import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

record Options(int nReaders, int nWriters, int udpPort, int packetSize) {}

public class Producer {
    private DatagramQueue queue;
    private Reader[] readers;
    private Writer[] writers;

    private static ExecutorService executorService;
    private static DatagramSocket readSocket;

    private final Options opts;

    public Producer(String[] args) {
        if  (args.length != 4) {
            this.opts = new Options(1, 1, 6006, 1024);
        } else {
            this.opts = new Options(
                    Integer.parseInt(args[0]), // number of reader threads
                    Integer.parseInt(args[1]), // number of writer threads
                    Integer.parseInt(args[2]), // udp port for readers
                    Integer.parseInt(args[3])  // datagram size
            );
        }

        executorService = Executors.newFixedThreadPool(opts.nReaders() + opts.nWriters());
        this.allocate(opts.udpPort());
    }

    public static void main(String[] args) {

        Producer kafkaProducer = new Producer(args);

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

    public void allocate(int readPort) {
        this.queue = new DatagramQueue(512);
        this.readers = new Reader[opts.nReaders()];
        this.writers = new Writer[opts.nWriters()];

        try {
            readSocket = new DatagramSocket(readPort);
        } catch (SocketException e) {
            System.out.println("SocketError: " + e.getMessage());
            Runtime.getRuntime().exit(1);
        }

        for (int i = 0; i < opts.nReaders(); i++) {
            this.readers[i] = new Reader(queue, readSocket, opts.packetSize());
        }

        for (int i = 0; i < opts.nWriters(); i++) {
            this.writers[i] = new Writer(queue);
        }
    }

    public void start() {
        for (Runnable r : this.readers) {
            executorService.execute(r);
        }
        for (Runnable w : this.writers) {
            executorService.execute(w);
        }
    }
}
