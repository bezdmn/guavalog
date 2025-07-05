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

    public Producer(int nReaders, int nWriters, int readPort) {
        this.nReaders = nReaders;
        this.nWriters = nWriters;
        executorService = Executors.newFixedThreadPool(nReaders + nWriters);
        this.allocate(readPort);
    }

    public static void main(String[] args) {
        Producer kafkaProducer = new Producer(2, 4, 1234);

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
