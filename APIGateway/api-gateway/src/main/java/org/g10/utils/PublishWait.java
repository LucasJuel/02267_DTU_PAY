package org.g10.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;
import com.google.gson.Gson;

public class PublishWait {
    private static final Gson gson = new Gson();
    private final String response;
    
    public PublishWait(String queue, String replyQueue, Channel channel, Object object){
        try{
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
            String response = responseFuture.get(20, java.util.concurrent.TimeUnit.SECONDS); // Wait for the response
            channel.basicCancel(consumerTag);
            this.response = response;
        } catch (java.util.concurrent.TimeoutException e){
            String errorMsg = String.format("Timeout waiting for response from queue '%s' after 20 seconds", queue);
            throw new RuntimeException(errorMsg, e);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            String errorMsg = String.format("Thread interrupted while waiting for response from queue '%s'", queue);
            throw new RuntimeException(errorMsg, e);
        } catch (java.util.concurrent.ExecutionException e){
            String errorMsg = String.format("Execution failed while waiting for response from queue '%s': %s", 
                queue, e.getCause() != null ? e.getCause().toString() : e.getClass().getName());
            throw new RuntimeException(errorMsg, e);
        } catch (IOException e){
            String errorMsg = String.format("I/O error during message publish/consume on queue '%s': %s", 
                queue, e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e){
            String errorMsg = String.format("Unexpected error during publish and wait on queue '%s': %s", 
                queue, e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            throw new RuntimeException(errorMsg, e);
        }
       
    }

    public String getResponse() {
        return response;
    }

    private static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}

