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

    public void clear() {
        pointer = 0;
    }

    public boolean isFull() {
        return pointer == buf.length;
    }
}
