package ru.merezh.paymentservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные о статусе оплаты")
public enum PaymentStatus {
    SUCCESS,
    WAITING,
    FAILED
}
