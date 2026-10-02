package ru.merezh.paymentservice.dto;

import java.math.BigDecimal;

public record WalletAmountDto(
        BigDecimal amount
) {
}
