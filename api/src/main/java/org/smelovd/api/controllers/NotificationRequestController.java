package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smelovd.api.entities.NotificationRequest;
import org.smelovd.api.factories.NotificationRequestFactory;
import org.smelovd.api.factories.NotificationTemplateFactory;
import org.smelovd.api.services.ProduceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/send")
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestFactory notificationRequestFactory;
    private final NotificationTemplateFactory notificationTemplateFactory;
    private final ProduceService produceService;

    @PostMapping("")
    public Mono<ResponseEntity<NotificationRequest>> parseAndProduce(@RequestPart("message") String message, @RequestPart("file") Mono<FilePart> file) {
        return notificationTemplateFactory.create(message, file)
                .flatMap(template -> {
                    log.info("Created template with id: {}", template.getId());
                    return notificationRequestFactory.create(template.getId(), message);
                })
                .flatMap(request -> {
                    log.info("Sending request with id: {}", request.getId());
                    return produceService.asyncParseAndProduce(request.getId())
                            .thenReturn(new ResponseEntity<>(request, HttpStatus.OK));
                });
    }

    @PostMapping("/{templateId}")
    public Mono<ResponseEntity<String>> produce(@PathVariable("templateId") String templateId, @RequestParam(value = "message", required = false) String message) {
        log.info("Send request with id: {}", templateId);
        return produceService.asyncProduce(templateId, message)
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }
}
