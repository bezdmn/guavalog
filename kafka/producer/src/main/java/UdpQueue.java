/*

 A double buffered ("ping-pong buffer") blocking queue for transient storage
 of UDP packets. The queue is backed by two buffers implemented with
 MemorySegments; these segments have a sequence layout, where each element
 matches the structure of a datagram packet as defined in UdpBuffer.

*/

import java.lang.foreign.MemorySegment;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class UdpQueue implements BlockingQueue<MemorySegment> {

    @Override
    public boolean add(MemorySegment memorySegment) {
        return false;
    }

    @Override
    public boolean offer(MemorySegment memorySegment) {
        return false;
    }

    @Override
    public MemorySegment remove() {
        return null;
    }

    @Override
    public MemorySegment poll() {
        return null;
    }

    @Override
    public MemorySegment element() {
        return null;
    }

    @Override
    public MemorySegment peek() {
        return null;
    }

    @Override
    public void put(MemorySegment memorySegment) throws InterruptedException {

    }

    @Override
    public boolean offer(MemorySegment memorySegment, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public MemorySegment take() throws InterruptedException {
        return null;
    }

    @Override
    public MemorySegment poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
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
    public boolean addAll(Collection<? extends MemorySegment> c) {
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
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<MemorySegment> iterator() {
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
    public int drainTo(Collection<? super MemorySegment> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super MemorySegment> c, int maxElements) {
        return 0;
    }
}
