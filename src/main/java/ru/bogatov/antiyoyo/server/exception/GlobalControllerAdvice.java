package ru.bogatov.antiyoyo.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> anyError(RuntimeException e) {
        return new ResponseEntity<>(ErrorResponse.from(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> applicationError(ApplicationException e) {
        return new ResponseEntity<>(e.getErrorResponse(), e.getErrorResponse().getStatus());
    }

}
