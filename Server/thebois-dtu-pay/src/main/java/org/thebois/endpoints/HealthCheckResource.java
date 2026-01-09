package org.thebois.endpoints;

import jakarta.ws.rs.Path;

public class HealthCheckResource {
    @Path("/health")
    public String healthCheck() {
        return "OK";
    }
}
