package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.services.RecoveryProduceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/send/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryProduceService recoveryProduceService;

    @PostMapping("/{requestId}")
    public Mono<ResponseEntity<String>> recoveryProduceRequest(
            @PathVariable("requestId") String requestId,
            @RequestParam("isParsed") boolean isParsed,
            @RequestParam("currentParsedCount") String currentParsedCount) {
        log.info("Send recovery notification with id: {}", requestId);
        if (isParsed) {
            return recoveryProduceService.asyncProduce(requestId)
                    .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        }
        return recoveryProduceService.asyncParseAndProduce(requestId, currentParsedCount)
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
