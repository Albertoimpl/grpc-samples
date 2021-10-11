package com.example.grpcgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class GrpcGatewayApplication {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplication.run(GrpcGatewayApplication.class, args);
    }

}
