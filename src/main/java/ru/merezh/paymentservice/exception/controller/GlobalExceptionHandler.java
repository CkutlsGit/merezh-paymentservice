package ru.merezh.paymentservice.exception.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import ru.merezh.paymentservice.exception.PaymentException;
import ru.merezh.paymentservice.exception.dto.ExceptionDto;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ExceptionDto> paymentExceptionHandler(PaymentException e) {
        return ResponseEntity.status(e.getCode()).body(new ExceptionDto(e.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> httpClientErrorExceptionHandler(HttpClientErrorException e) {
        log.info("Ошибка класса клиента - {}: {}", e.getClass(), e.getMessage());

        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> httpServerErrorExceptionHandler(HttpServerErrorException e) {
        log.info("Ошибка класса сервера - {}: {}", e.getClass(), e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> exceptionHandler(Exception e) {
        log.info("Ошибка неизвестного класса {} - {}", e.getClass(), e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionDto("Ошибка сервиса"));
    }
}
