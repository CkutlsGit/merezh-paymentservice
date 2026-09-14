package ru.merezh.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.merezh.paymentservice.dto.OrderDto;
import ru.merezh.paymentservice.dto.WalletRequestDto;
import ru.merezh.paymentservice.entity.Payment;
import ru.merezh.paymentservice.entity.PaymentStatus;
import ru.merezh.paymentservice.exception.PaymentException;
import ru.merezh.paymentservice.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${service.wallets.url}")
    private String baseWalletUrl;

    @Transactional(noRollbackFor = {HttpClientErrorException.class, HttpServerErrorException.class})
    public PaymentStatus placeOrder(OrderDto orderDto) {
        Optional<Payment> payment = paymentRepository.getPaymentByOrderId(orderDto.orderId());
        if (payment.isPresent()) {
            return existsOrder(payment.get());
        }

        Payment paymentCreated = new Payment();

        paymentCreated.setOrderId(orderDto.orderId());
        paymentCreated.setUserId(orderDto.userId());
        paymentCreated.setTotalAmount(orderDto.totalAmount());
        paymentCreated.setStatus(PaymentStatus.WAITING);
        paymentCreated.setDate(orderDto.date());

        paymentRepository.save(paymentCreated);

        return paymentCreated.getStatus();
    }

    @Transactional(noRollbackFor = {HttpClientErrorException.class, HttpServerErrorException.class})
    public PaymentStatus payOrder(long orderId, long userId) {
        Payment payment = paymentRepository.getPaymentByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Заказ не найден" ,HttpStatus.NOT_FOUND));

        if (payment.getUserId() != userId) {
            throw new PaymentException("Нет доступа к заказу");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return payment.getStatus();
        }

        try {
            PaymentStatus paymentStatus = sendRequestWallet(new WalletRequestDto(payment.getUserId(), payment.getTotalAmount()));

            payment.setStatus(paymentStatus);
            return payment.getStatus();
        }
        catch (HttpClientErrorException e) {
            payment.setStatus(PaymentStatus.FAILED);
            throw e;
        }
        catch (HttpServerErrorException e) {
            payment.setStatus(PaymentStatus.WAITING);
            throw e;
        }
    }

    private PaymentStatus sendRequestWallet(WalletRequestDto walletRequestDto) throws HttpClientErrorException, HttpServerErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", String.valueOf(walletRequestDto.userId()));

        HttpEntity<BigDecimal> request = new HttpEntity<>(walletRequestDto.totalAmount(), headers);

        ResponseEntity<BigDecimal> response = restTemplate.exchange(
                baseWalletUrl + "/balance/sub",
                HttpMethod.POST,
                request,
                BigDecimal.class
        );

        return PaymentStatus.SUCCESS;
    }

    private PaymentStatus existsOrder(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return payment.getStatus();
        }

        try {
            return sendRequestWallet(new WalletRequestDto(payment.getUserId(), payment.getTotalAmount()));
        }
        catch (HttpClientErrorException e) {
            payment.setStatus(PaymentStatus.FAILED);
            throw e;
        }
        catch (HttpServerErrorException e) {
            payment.setStatus(PaymentStatus.WAITING);
            throw e;
        }
    }
}
