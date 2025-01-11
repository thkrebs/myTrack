package com.tmv.core.controller;

import com.tmv.core.util.AccessViolationException;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class BaseController {

    @ExceptionHandler(AccessViolationException.class)
    ResponseEntity<ErrorMessage> accessViolationHandler(Exception exception) {
        return new ResponseEntity<>(
                new ErrorMessage(exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @Value
    private static class ErrorMessage {
        String message;
    }
}
