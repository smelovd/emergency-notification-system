package org.smelovd.api.controllers;

import lombok.RequiredArgsConstructor;
import org.smelovd.api.entities.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.*;

@RestController
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationRequestController notificationRequestController;

    @PostMapping("/send-test-notification")
    public Mono<ResponseEntity<NotificationRequest>> sendTestNotification(@RequestParam("count_user") Integer count, @RequestParam("message") String message) {
        MultipartFile file = createFile(count);
        return notificationRequestController.saveNotificationRequest(message, file);
    }

    private MultipartFile createFile(Integer count) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= count; i++) {
            builder.append(i).append(",").append(i).append("@test.com,").append("TEST\n");
        }
        return new MultipartFile() {

            private final byte[] input = builder.toString().getBytes();

            @Override
            public String getName() {
                return "testUser.csv";
            }

            @Override
            public String getOriginalFilename() {
                return "testUser.csv";
            }

            @Override
            public String getContentType() {
                return "text/csv";
            }

            @Override
            public boolean isEmpty() {
                return input.length == 0;
            }

            @Override
            public long getSize() {
                return input.length;
            }

            @Override
            public byte[] getBytes() {
                return input;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(input);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(input);
                }
            }
        };
    }
}
