public class DatagramBuffer {
    private final Datagram[] buf;
    private int pointer;

    public DatagramBuffer(int size) {
        this.buf = new Datagram[size];
        this.pointer = 0;
    }

    public boolean add(Datagram d) {
        if (pointer < buf.length) {
            buf[pointer] = d;
            pointer++;
            return true;
        }
        return false;
    }

    public Datagram take() {
        if (pointer > 0) {
            pointer--;
            return buf[pointer];
        }
        return null;
    }

    /**
     * Reset the pointer to zero, effectively clearing the buffer.
     *
     * @return the number of elements still left in the buffer.
     */
    public int clear() {
        int temp = pointer;
        pointer = 0;
        return temp;
    }

    public int size() { return buf.length; }
    public boolean isFull() {
        return pointer == buf.length;
    }
}
