package org.smelovd.checker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckerApplication.class, args);
    }

}
