package com.rabbitmq.example6;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@EnableAutoConfiguration
@ComponentScan
@Import(RabbitConfiguration.class)
public class ConsumerApplication {
    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

        SpringApplication app = new SpringApplication(ConsumerApplication.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", "0"));
        app.run(args);
    }
}