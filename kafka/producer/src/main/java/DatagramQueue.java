import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatagramQueue implements BlockingQueue<Datagram> {
    private final DatagramBuffer bufferA;
    private final DatagramBuffer bufferB;

    /**
     * A double buffered ("ping-pong buffer") blocking queue for transient storage
     * of UDP packets. The queue is backed by two arrays with Datagram type and is
     * meant to be used by threads that might block.
     *
     * @param size sizes of the underlying arrays
     */
    public DatagramQueue(int size) {
        this.bufferA = new DatagramBuffer(size);
        this.bufferB = new DatagramBuffer(size);
    }

    // Primary Override methods

    @Override
    public Datagram take() throws InterruptedException {
        return null;
    }

    @Override
    public void put(Datagram datagram) throws InterruptedException {

    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    // Secondary Override methods

    @Override
    public boolean add(Datagram datagram) {
        return false;
    }

    @Override
    public boolean offer(Datagram datagram) {
        return false;
    }

    @Override
    public Datagram remove() {
        return null;
    }

    @Override
    public Datagram poll() {
        return null;
    }

    @Override
    public Datagram element() {
        return null;
    }

    @Override
    public Datagram peek() {
        return null;
    }

    @Override
    public boolean offer(Datagram datagram, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Datagram poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Datagram> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Datagram> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public int drainTo(Collection<? super Datagram> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super Datagram> c, int maxElements) {
        return 0;
    }
}
