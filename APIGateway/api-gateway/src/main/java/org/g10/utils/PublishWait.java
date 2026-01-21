package org.g10.utils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;
import com.google.gson.Gson;

public class PublishWait {
    private static final Gson gson = new Gson();
    private final String response;
    
    public PublishWait(String queue, String replyQueue, Channel channel, Object object) throws Exception {
        String correlationId = java.util.UUID.randomUUID().toString();
        String payload = toJson(object);
        String replyQueueName = channel.queueDeclare(replyQueue, false, true, true, null).getQueue();
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        String consumerTag = channel.basicConsume(replyQueueName, true, (tag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            responseFuture.complete(message);
        }, tag -> {});

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .correlationId(correlationId)
                .deliveryMode(2)
                .replyTo(replyQueue)
                .build();
    
        channel.basicPublish("", queue, props, payload.getBytes(StandardCharsets.UTF_8));
        String response = responseFuture.get(5, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
        channel.basicCancel(consumerTag);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    private static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}


