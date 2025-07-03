public class Producer {
    private UdpQueue queue;
    private Read reader;
    private Write writer;

    public static void main(String[] args) {
        Producer kafkaProducer = new Producer();

        kafkaProducer.allocate();
    }

    public void allocate() {
        this.queue = new UdpQueue();
        this.reader = new Read();
        this.writer = new Write();
    }
}
