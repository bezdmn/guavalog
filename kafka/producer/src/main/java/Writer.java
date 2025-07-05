import java.net.DatagramSocket;

public class Writer implements Runnable {
    private final DatagramQueue queue;
    private volatile boolean running;

    public Writer(DatagramQueue queue) {
        this.queue = queue;
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("WriterError: " + e.getMessage());
            }
        }
    }
}
