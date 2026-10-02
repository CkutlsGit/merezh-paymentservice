package ru.merezh.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "Данные для создания объекта оплаты")
public record OrderDto(

        @Schema(description = "Индефикатор заказа")
        long orderId,

        @Schema(description = "Индефикатор пользователя")
        long userId,

        @Schema(description = "Итоговая сумма оплаты заказа")
        BigDecimal totalAmount,

        @Schema(description = "Дата заказа")
        Date date
) {
}
