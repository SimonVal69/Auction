package com.example.auction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.sql.SQLException;

@RestControllerAdvice
public class AuctionExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<String> handleIOException(IOException ioException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка ввода/вывода: " + ioException.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleSQLException(SQLException sqlException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка SQL: " + sqlException.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка приложения/сервера: " + exception.getMessage());
    }
}
