package com.cred.search.server.handler;

import com.cred.search.core.exception.SearchServiceException;
import com.cred.search.models.response.wrapper.ApiResponse;
import com.cred.search.models.response.wrapper.ApiStatusCode;
import com.cred.search.models.response.wrapper.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static void logError(Exception exception) {
        log.error("Exception:<{}> Mapped to {}", exception.getClass().getSimpleName(), GlobalExceptionHandler.class.getSimpleName(), exception);
    }


    @ExceptionHandler(SearchServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleSearchServiceException(SearchServiceException exception) {
        logError(exception);

        ApiResponse<Object> response = ApiResponse.builder()
                .status(ResponseStatus.FAILURE)
                .responseMessage(exception.getMessage())
                .responseCode(ApiStatusCode.UNEXPECTED_RUN_TIME_EXCEPTION.getErrorCode())
                .data(null)
                .build();
        return ResponseEntity.status(ApiStatusCode.UNEXPECTED_RUN_TIME_EXCEPTION.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRunTimeException(RuntimeException exception) {
        logError(exception);

        ApiResponse<Object> response = ApiResponse.builder()
                .status(ResponseStatus.FAILURE)
                .responseMessage(exception.getMessage())
                .responseCode(ApiStatusCode.UNEXPECTED_RUN_TIME_EXCEPTION.getErrorCode())
                .data(null)
                .build();
        return ResponseEntity.status(ApiStatusCode.UNEXPECTED_RUN_TIME_EXCEPTION.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(response);
    }

}
