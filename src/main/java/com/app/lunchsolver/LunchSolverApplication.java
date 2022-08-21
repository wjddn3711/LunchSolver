package com.app.lunchsolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LunchSolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunchSolverApplication.class, args);
    }

}
