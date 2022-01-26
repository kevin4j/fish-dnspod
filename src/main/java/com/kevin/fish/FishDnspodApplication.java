package com.kevin.fish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FishDnspodApplication {

    public static void main(String[] args) {
        SpringApplication.run(FishDnspodApplication.class, args);
    }

}
