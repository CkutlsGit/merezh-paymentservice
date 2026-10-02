package ru.merezh.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.merezh.paymentservice.dto.OrderDto;
import ru.merezh.paymentservice.entity.Payment;
import ru.merezh.paymentservice.entity.PaymentStatus;
import ru.merezh.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/get/{id}")
    @Operation(summary = "Метод получение оплаты по id")
    public ResponseEntity<Payment> getPayment(@PathVariable long id) {
        return ResponseEntity.ok().body(paymentService.getPayment(id));
    }

    @GetMapping("/get/user")
    @Operation(summary = "Метод получение истории оплаты пользователя по его id")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@RequestHeader("X-User-Id") long userId) {
        return ResponseEntity.ok().body(paymentService.getPaymentsByUserId(userId));
    }

    @PostMapping("/place")
    @Operation(summary = "Метод для создания объекта оплаты")
    public ResponseEntity<PaymentStatus> placeOrder(@RequestBody OrderDto orderData) {
        return ResponseEntity.ok().body(paymentService.placeOrder(orderData));
    }

    @PostMapping("/pay/{orderId}")
    @Operation(summary = "Метод для оплаты заказа по его id")
    public ResponseEntity<PaymentStatus> payOrder(@PathVariable long orderId, @RequestHeader("X-User-Id") long userId) {
        return ResponseEntity.ok().body(paymentService.payOrder(orderId, userId));
    }
}
