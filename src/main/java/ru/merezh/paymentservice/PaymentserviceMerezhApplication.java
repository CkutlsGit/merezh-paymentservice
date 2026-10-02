package ru.merezh.paymentservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Paymentservice Merezh",
                version = "v1.0",
                description = "Эндпоинты для создания и проведение оплаты заказа"
        )
)
@SpringBootApplication
public class PaymentserviceMerezhApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentserviceMerezhApplication.class, args);
	}

}
