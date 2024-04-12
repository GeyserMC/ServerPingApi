package org.geysermc.serverpingapi;

import org.geysermc.serverpingapi.models.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class ResponseExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> endpointNotFound(final NoHandlerFoundException exception) {
        return this.error(HttpStatus.NOT_FOUND, "Endpoint not found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(final Exception exception) {
        return this.error(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(final HttpStatus status, final String error) {
        return new ResponseEntity<>(
            new ErrorResponse(error),
            status
        );
    }
}
