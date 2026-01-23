package org.g10.services;
/**
 @author gh05tdog
 **/
public class AccountServiceApplication {
    private static AccountConsumer consumer;
    public static void main(String[] args) {
        System.out.println("Starting Account Service Application...");
        try{
            // Thread.sleep(5000);
            consumer = new AccountConsumer();
            consumer.startListening();
            Thread.currentThread().join();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public AccountConsumer getConsumer(){
        return consumer;
    }
}
