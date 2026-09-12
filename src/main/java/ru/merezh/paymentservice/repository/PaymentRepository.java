package ru.merezh.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.merezh.paymentservice.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> getPaymentByOrderId(long id);
}
