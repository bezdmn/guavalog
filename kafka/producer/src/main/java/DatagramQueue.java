
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DatagramQueue implements BlockingQueue<Datagram> {
    private final DatagramBuffer[] buffer;

    private volatile int readBuf = 0;
    private int writeBuf = 1;

    private final ReentrantLock writeLock;
    private final Condition isFull;

    /**
     * A bounded, double buffered ("ping-pong buffer") blocking queue for transient
     * storage of UDP packets. The queue is backed by an array of two buffers, where
     * the front buffer is read from and the back buffer is written to. The buffers
     * are switched when the front is empty and the back is full.
     *
     * @param size sizes of the underlying arrays
     */
    public DatagramQueue(int size) {
        this.buffer = new DatagramBuffer[2];
        this.buffer[readBuf] = new DatagramBuffer(size);
        this.buffer[writeBuf] = new DatagramBuffer(size);
        this.writeLock = new ReentrantLock();
        this.isFull = this.writeLock.newCondition();
    }

    // Primary Override methods

    @Override
    public Datagram take() throws InterruptedException {
        Datagram temp = buffer[readBuf].take();
        if (temp == null) {
            isFull.await();
            return buffer[readBuf].take();
        }
        return temp;
    }

    /**
     * Insert a new datagram at the head of the buffer. If the buffer is full,
     * do a rotation by XORing the write/read indices. The next writeBuf can
     * still have unprocessed elements if the readers are slower than the writers;
     * ignore those elements and clear the buffer anyway.
     *
     * @param datagram the element to add
     * @throws InterruptedException
     */
    @Override
    public void put(Datagram datagram) throws InterruptedException {
        writeLock.lockInterruptibly();
        try {
            if (buffer[writeBuf].isFull()) {
                // Swap the buffers and clear the new writeBuffer just in case.
                writeBuf ^= 1;
                readBuf ^= 1;
                buffer[writeBuf].clear();
                // Signal any readers that there are new datagrams available
                isFull.signal();
            }
            buffer[writeBuf].add(datagram);
        } finally {
            writeLock.unlock();
        }
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
