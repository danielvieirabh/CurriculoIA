package com.example.curriculoia;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CurriculoIaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurriculoIaApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            String porta = environment.getProperty("local.server.port");
            System.out.println("O servidor está rodando na porta: " + porta);
        };
    }
}
