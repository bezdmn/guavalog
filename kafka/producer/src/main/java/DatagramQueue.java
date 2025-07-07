import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DatagramQueue implements BlockingQueue<byte[]> {
    private final DatagramBuffer[] buffer;
    private final AtomicInteger count;
    private final ReentrantLock writeLock;
    private final Condition isFull;

    /* Volatile readBuf avoids read-threads from caching the value after buffer swap */
    private volatile int readBuf = 0;
    private int writeBuf = 1;

    /**
     * A bounded, double buffered ("ping-pong buffer") concurrent queue for transient
     * storage of UDP packets. The queue is backed by an array of two buffers, where
     * the front buffer is read from and the back buffer is written to. The buffers
     * are switched when the back is full, even if the front has elements remaining.
     * Reading from the queue is semi lock-free; writing to the queue is blocking.
     *
     * @param bufferSize sizes of the underlying arrays
     */
    public DatagramQueue(int bufferSize) {
        this.buffer = new DatagramBuffer[2];
        this.buffer[readBuf] = new DatagramBuffer(bufferSize);
        this.buffer[writeBuf] = new DatagramBuffer(bufferSize);
        this.count = new AtomicInteger(0);
        this.writeLock = new ReentrantLock();
        this.isFull = this.writeLock.newCondition();
    }

    // Primary Override methods

    /**
     * Taking values from the buffer is done lock-free. The atomic guarantees
     * that all elements are read only once from the buffer, but without any
     * guarantee on the order they're read. Threads enter sleep after reading
     * all elements and wait for a reader thread to swap buffers and notify.
     *
     * @return A datagram class object
     * @throws InterruptedException The sleep can be interrupted by system
     */
    @Override
    public byte[] take() throws InterruptedException {
        System.out.println("Count: "+ count.get());
        while (count.incrementAndGet() >= buffer[readBuf].size()) {
            isFull.await();
        }
        /*while ((temp = buffer[readBuf].take()) == null) {
            isFull.await();
        }*/
        return buffer[readBuf].take();
    }

    /**
     * Insert a new datagram at the head of the buffer. If the buffer is full,
     * do a rotation by XORing the write/read indices. The next writeBuf can
     * still have unprocessed elements if the readers are slower than the writers;
     * however, writing doesn't stop to wait and the buffer is always cleared.
     *
     * @param datagram the element to add
     * @throws InterruptedException method was interrupted by system
     */
    @Override
    public void put(byte[] datagram) throws InterruptedException {
        writeLock.lock();
        try {
            if (buffer[writeBuf].isFull()) {
                // Swap the buffers
                writeBuf ^= 1;
                readBuf ^= 1;
                // Reset count and buffer, signal all readers to continue
                buffer[writeBuf].clear();
                count.set(0);
                isFull.signalAll();
            }
            buffer[writeBuf].add(datagram);
        } finally {
            writeLock.unlock();
        }
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
    public byte[] poll(long timeout, TimeUnit unit) throws InterruptedException {
        return new byte[0];
    }

    @Override
    public boolean add(byte[] bytes) {
        return false;
    }

    @Override
    public boolean offer(byte[] bytes) {
        return false;
    }

    @Override
    public byte[] remove() {
        return new byte[0];
    }

    @Override
    public byte[] poll() {
        return new byte[0];
    }

    @Override
    public byte[] element() {
        return new byte[0];
    }

    @Override
    public byte[] peek() {
        return new byte[0];
    }

    @Override
    public boolean offer(byte[] bytes, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public int remainingCapacity() {
        return 0;
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
    public boolean addAll(Collection<? extends byte[]> c) {
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
    public Iterator<byte[]> iterator() {
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
    public int drainTo(Collection<? super byte[]> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super byte[]> c, int maxElements) {
        return 0;
    }

}
