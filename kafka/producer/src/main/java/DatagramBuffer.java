public class DatagramBuffer {
    private final Datagram[] buf;
    private int pointer;
    private boolean emptying;

    public DatagramBuffer(int size) {
        this.buf = new Datagram[size];
        this.pointer = 0;
        this.emptying = false;
    }

    public boolean add(Datagram d) {
        if (pointer < buf.length) {
            buf[pointer] = d;
            pointer++;
            return true;
        }

        emptying = true;
        return false;
    }

    public Datagram take() {
        if (pointer > 0 && emptying) {
            pointer--;

            if (pointer == 0) {
                emptying = false;
            }

            return buf[pointer];
        }

        return null;
    }

    public boolean isEmptying() {
        return emptying;
    }

    public boolean isFull() {
        return pointer == buf.length;
    }

}
