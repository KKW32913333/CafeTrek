package com.cafetrek.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class WebPushConfig {

    static {
        // web-push-java needs BouncyCastle for the VAPID/ECDSA crypto.
        Security.addProvider(new BouncyCastleProvider());
    }

    @Value("${webpush.public-key}")
    private String publicKey;

    @Value("${webpush.private-key}")
    private String privateKey;

    @Value("${webpush.subject}")
    private String subject;

    @Bean
    public PushService pushService() throws Exception {
        PushService service = new PushService();
        if (!publicKey.isBlank() && !privateKey.isBlank()) {
            service.setPublicKey(publicKey);
            service.setPrivateKey(privateKey);
            service.setSubject(subject);
        }
        // If keys are blank (not configured yet), the bean still exists but
        // sends will fail loudly — see WebPushService, which logs and skips
        // instead of crashing the scheduler.
        return service;
    }
}
