package ru.merezh.paymentservice.dto;

import ru.merezh.paymentservice.entity.PaymentStatus;

public record OrderUpdateDto(
        long orderId,
        PaymentStatus status
) {
}
