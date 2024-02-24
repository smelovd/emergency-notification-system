package org.smelovd.test_endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

@Controller
public class TestController {

    @PostMapping("/test")
    public Mono<ResponseEntity<String>> send() {
        return Mono.just(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
