package ru.merezh.paymentservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Объект представляющий данные о заказе")
public class Payment {

    @Id
    private Long orderId;

    private Long userId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Date date;
}
