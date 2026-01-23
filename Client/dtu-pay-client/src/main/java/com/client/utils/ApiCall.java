package com.client.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 @author TheZoap
 **/

public class ApiCall {
    private final HttpClient httpClient;
    private final String baseUrl;

    public ApiCall(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Performs a GET request to the specified endpoint
     */
    public HttpResponse<String> get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    /**
     * Performs a POST request to the specified endpoint with JSON body
     */
    public HttpResponse<String> post(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    /**
     * Performs a PUT request to the specified endpoint with JSON body
     */
    public HttpResponse<String> put(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    /**
     * Performs a DELETE request to the specified endpoint
     */
    public HttpResponse<String> delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    /**
     * Gets the response with status code
     */
    public HttpResponse<String> getWithResponse(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Posts with response including status code
     */
    public HttpResponse<String> postWithResponse(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}