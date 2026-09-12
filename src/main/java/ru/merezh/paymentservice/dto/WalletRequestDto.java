package ru.merezh.paymentservice.dto;

import java.math.BigDecimal;

public record WalletRequestDto(
        long userId,
        BigDecimal totalAmount
) {
}
