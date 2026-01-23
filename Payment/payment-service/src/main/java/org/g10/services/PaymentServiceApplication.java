package org.g10.services;
/**
 @author ssschoubye
 **/
public class PaymentServiceApplication {
    private static PaymentConsumer consumer;
    public static void main(String[] args) {
        System.out.println("Starting Payment Service Application...");
        try{
            // Thread.sleep(5000);
            consumer = new PaymentConsumer();
            consumer.startListening();
            Thread.currentThread().join();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public PaymentConsumer getConsumer(){
        return consumer;
    }
}
