package org.g10.endpoints;

import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class AbstractResource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected <T> Response handleRegister(T request, Supplier<Response> serviceCall) {
        logger.info("Register request: {}", request);
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"request body is required.\"}")
                    .build();
        }
        try {
            return serviceCall.get();
        } catch (Exception e) {
            logger.error("Registration failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
