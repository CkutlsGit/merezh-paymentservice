package ru.merezh.paymentservice.dto;

import java.math.BigDecimal;
import java.util.Date;

public record OrderDto(
        long orderId,
        long userId,
        BigDecimal totalAmount,
        Date date
) {
}
