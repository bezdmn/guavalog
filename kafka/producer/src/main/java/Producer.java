import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Producer {
    private DatagramQueue queue;
    private Reader[] readers;
    private Writer[] writers;
    private static ExecutorService executorService;
    private static DatagramSocket readSocket;

    private final int nReaders;
    private final int nWriters;
    private final int readPort;

    public Producer(String[] args) {
        if  (args.length != 3) {
            this.nReaders = 1;
            this.nWriters = 1;
            this.readPort = 6006;
        } else {
            this.nReaders = Integer.parseInt(args[0]);
            this.nWriters = Integer.parseInt(args[1]);
            this.readPort = Integer.parseInt(args[2]);
        }

        executorService = Executors.newFixedThreadPool(nReaders + nWriters);
        this.allocate(readPort);
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
        this.readers = new Reader[nReaders];
        this.writers = new Writer[nWriters];

        try {
            readSocket = new DatagramSocket(readPort);
        } catch (SocketException e) {
            System.out.println("SocketError: " + e.getMessage());
            Runtime.getRuntime().exit(1);
        }

        for (int i = 0; i < nReaders; i++) {
            this.readers[i] = new Reader(queue, readSocket, 1024);
        }

        for (int i = 0; i < nWriters; i++) {
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
