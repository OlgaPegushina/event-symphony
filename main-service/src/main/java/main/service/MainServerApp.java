package main.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"main.service", "stats/client"})
public class MainServerApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServerApp.class, args);
    }
}