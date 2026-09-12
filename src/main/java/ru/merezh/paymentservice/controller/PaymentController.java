package ru.merezh.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.merezh.paymentservice.dto.OrderDto;
import ru.merezh.paymentservice.entity.PaymentStatus;
import ru.merezh.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/place")
    public ResponseEntity<PaymentStatus> placeOrder(@RequestBody OrderDto orderData) {
        return ResponseEntity.ok().body(paymentService.placeOrder(orderData));
    }

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<PaymentStatus> payOrder(@PathVariable long orderId, @RequestHeader("X-User-Id") long userId) {
        return ResponseEntity.ok().body(paymentService.payOrder(orderId, userId));
    }
}
