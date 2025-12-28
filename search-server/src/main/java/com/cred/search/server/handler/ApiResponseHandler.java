package com.cred.search.server.handler;

import com.cred.search.models.response.wrapper.ApiResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;


@Slf4j
@UtilityClass
public final class ApiResponseHandler {

    public static <T> ResponseEntity<ApiResponse<T>> handle(String operationName, Object request, Supplier<T> action) {
        log.info("Received request for {}: {}", operationName, request);

        T response = action.get();

        log.info("Sending response for {}: {}", operationName, response);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}