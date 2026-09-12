FROM eclipse-temurin:21-alpine

WORKDIR /app

RUN addgroup -S paymentservice && adduser -S paymentservice -G paymentservice

COPY target/paymentservice-merezh-0.0.1-SNAPSHOT.jar /app/paymentservice-merezh.jar

RUN chown -R paymentservice:paymentservice /app

USER paymentservice

ENTRYPOINT ["java", "-jar", "paymentservice-merezh.jar"]