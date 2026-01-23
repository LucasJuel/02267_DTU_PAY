package org.g10.services;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
/**
 @author gh05tdog
 **/
@ApplicationScoped
public class PaymentConsumerStarter {
    private PaymentConsumer consumer;

    void onStart(@Observes StartupEvent ev) {
        new Thread(() -> {
            int maxRetries = 10;
            int retryDelay = 2000; // 2 seconds
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    consumer = new PaymentConsumer();
                    consumer.startListening();
                    return; // Success, exit the thread
                } catch (Exception e) {
                    System.err.println("Failed to start PaymentConsumer (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            System.err.println("Retry interrupted, giving up.");
                            return;
                        }
                    } else {
                        System.err.println("All retry attempts failed. PaymentConsumer could not be started.");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    void onStop(@Observes ShutdownEvent ev) throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
