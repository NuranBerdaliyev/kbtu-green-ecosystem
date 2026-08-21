package com.example.green;

import com.example.green.config.AuthProperties;
import com.example.green.config.GamificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({AuthProperties.class, GamificationProperties.class}) //Enable Auth properties class for jwt parameters
@SpringBootApplication//here Spring starts to run
public class GreenApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenApplication.class, args);
    }
}

