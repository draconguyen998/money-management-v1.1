package com.draco.moneymanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name:Money Manager}")
    private String fromName;

    private final WebClient webClient;

    public EmailService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendEmail(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        webClient.post()
                .uri("/smtp/email")
                .header("api-key", apiKey)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachmentBytes,
            String filename
    ) {
        if (attachmentBytes == null || attachmentBytes.length == 0) {
            throw new IllegalArgumentException("Attachment is empty");
        }
        if (filename == null || filename.isBlank()) {
            filename = "attachment.bin";
        }

        String base64 = Base64.getEncoder().encodeToString(attachmentBytes);

        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body,
                "attachment", List.of(
                        Map.of(
                                "content", base64,
                                "name", filename
                        )
                )
        );

        try {
            webClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Send mail with attachment failed: " + e.getMessage(), e);
        }
    }
}