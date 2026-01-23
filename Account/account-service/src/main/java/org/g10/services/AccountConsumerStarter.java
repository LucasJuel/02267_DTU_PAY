package org.g10.services;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
/**
 @author LucasJuel
 **/
@ApplicationScoped
public class AccountConsumerStarter {
    private AccountConsumer consumer;

    void onStart(@Observes StartupEvent ev) {
        new Thread(() -> {
            int maxRetries = 10;
            int retryDelay = 2000; // 2 seconds
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
     
                    consumer = new AccountConsumer();
                    consumer.startListening();
                 
                    return; // Success, exit the thread
                } catch (Exception e) {
                    
                    if (attempt < maxRetries) {
                        try {
                        
                            Thread.sleep(retryDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                     
                            return;
                        }
                    } else {
                     
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
