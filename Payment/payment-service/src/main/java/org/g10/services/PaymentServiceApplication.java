package main.java.org.g10.services;

public class PaymentServiceApplication {
    public static void main(String[] args) {
        System.out.println("Starting Payment Service Application...");
        try{
            Thread.sleep(5000);
            PaymentConsumer consumer = new PaymentConsumer();
            consumer.startListening();
            Thread.currentThread().join();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
