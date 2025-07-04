
public class Write implements Runnable {
    private volatile boolean running = true;

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {

    }
}
