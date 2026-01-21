package org.g10.services;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class AccountConsumerStarter {
    private AccountConsumer consumer;

    void onStart(@Observes StartupEvent ev) throws Exception {
        consumer = new AccountConsumer();
        consumer.startListening();
    }

    void onStop(@Observes ShutdownEvent ev) throws Exception {
        if (consumer != null) {
            consumer.close();
        }
    }
}
