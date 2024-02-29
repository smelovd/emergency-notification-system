package org.smelovd.test_endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Controller
public class TestController {

    private final Random random = new Random();
    private final AtomicLong requestAcceptedCount = new AtomicLong(0);

    @PostMapping("/test")
    public Mono<ResponseEntity<String>> send(@RequestParam("userId") String userId, @RequestParam("message") String message) {
        if (random.nextInt() % 3 == 1) return Mono.just(new ResponseEntity<>("something with server", HttpStatus.INTERNAL_SERVER_ERROR));
        log.info(requestAcceptedCount.incrementAndGet() + " " + userId);
        return Mono.just(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
