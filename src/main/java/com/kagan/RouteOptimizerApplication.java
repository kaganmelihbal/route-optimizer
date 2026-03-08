package com.kagan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The primary entry point for the Route Optimizer application.
 * This class initializes the Spring ApplicationContext and bootstraps 
 * the embedded web server to host the routing API.
 */
@SpringBootApplication
public class RouteOptimizerApplication {

    /**
     * Bootstraps the application environment.
     * Launches the Spring container, performs component scanning, and 
     * initializes the graph-based routing services.
     * * @param args Command-line arguments for configuration overrides.
     */
    public static void main(String[] args) {
        SpringApplication.run(RouteOptimizerApplication.class, args);
    }
}