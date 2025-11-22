package com.beachbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe principale dell'applicazione Beach Booking.
 *
 * Sistema multi-tenant per la gestione di prenotazioni
 * di ombrelloni per stabilimenti balneari.
 */
@SpringBootApplication
@EnableScheduling
public class BeachBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeachBookingApplication.class, args);

        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║                                                       ║\n" +
                "║      🏖️  BEACH BOOKING API - STARTED! 🏖️            ║\n" +
                "║                                                       ║\n" +
                "║  Multi-Tenant Beach Booking Management System        ║\n" +
                "║                                                       ║\n" +
                "║  API Documentation:                                  ║\n" +
                "║  http://localhost:8080/swagger-ui.html               ║\n" +
                "║                                                       ║\n" +
                "║  Health Check:                                       ║\n" +
                "║  http://localhost:8080/health                        ║\n" +
                "║                                                       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n"
        );
    }
}