package ru.merezh.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentException extends RuntimeException {

    private HttpStatus code;

    public PaymentException(String message) {
        super(message);
        this.code = HttpStatus.BAD_REQUEST;
    }

    public PaymentException(String message, HttpStatus code) {
        super(message);
        this.code = code;
    }
}
