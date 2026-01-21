package org.g10.Services;

public class TokenServiceApplication {
    private static TokenConsumer consumer;
    public static void main(String[] args) {
        System.out.println("Starting Account Service Application...");
        try{
            consumer = new TokenConsumer();
            consumer.startListening();
            Thread.currentThread().join();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public TokenConsumer getConsumer(){
        return consumer;
    }
}
