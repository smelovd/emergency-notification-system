package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/send/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final NotificationService notificationService;

    @PostMapping("/{requestId}")
    public Mono<ResponseEntity<String>> recoveryProduceRequest(
            @PathVariable("requestId") String requestId,
            @RequestParam(value = "isParsed", required = false, defaultValue = "false") boolean isParsed,
            @RequestParam(value = "currentParsedCount", required = false, defaultValue = "0") String currentParsedCount) {
        log.info("Send recovery notification with id: {}", requestId);
        if (isParsed) {
            return notificationService.asyncRecoveryProduce(requestId)
                    .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        }
        return notificationService.asyncRecoveryParseAndProduce(requestId, currentParsedCount)
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
