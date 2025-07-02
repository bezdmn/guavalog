package com.guavalog.kafka.producer;

public class Producer {
    private MsgBuf bufA;
    private MsgBuf bufB;

    public static void main(String[] args) {
        Producer kafkaProducer = new Producer();

        try {
            kafkaProducer.allocate();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void allocate() {
        this.bufA = new MsgBuf();
        this.bufB = new MsgBuf();
    }
}
