public class Producer {
    private UdpBuffer bufA;
    private UdpBuffer bufB;

    public static void main(String[] args) {
        Producer kafkaProducer = new Producer();

        try {
            kafkaProducer.allocate();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void allocate() {
        this.bufA = new UdpBuffer();
        this.bufB = new UdpBuffer();
    }
}
