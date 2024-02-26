package org.smelovd.test_endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.util.Random;

@Controller
public class TestController {
    private final Random random = new Random();

    @PostMapping("/test")
    public Mono<ResponseEntity<String>> send() {
        System.out.println(random.nextInt());
        return Mono.just(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
