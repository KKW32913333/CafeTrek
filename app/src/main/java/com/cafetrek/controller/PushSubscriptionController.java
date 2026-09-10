package com.cafetrek.controller;

import com.cafetrek.domain.PushSubscription;
import com.cafetrek.domain.User;
import com.cafetrek.repository.PushSubscriptionRepository;
import com.cafetrek.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Backs the service worker's push-subscription flow (see static/js/push.js). */
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final CurrentUserService currentUserService;

    @Value("${webpush.public-key}")
    private String publicKey;

    @GetMapping("/public-key")
    public Map<String, String> publicKey() {
        return Map.of("publicKey", publicKey);
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubscribeRequest req, Authentication auth) {
        User user = currentUserService.get(auth);
        PushSubscription sub = pushSubscriptionRepository.findByEndpoint(req.endpoint).orElseGet(PushSubscription::new);
        sub.setUser(user);
        sub.setEndpoint(req.endpoint);
        sub.setP256dh(req.keys.p256dh);
        sub.setAuth(req.keys.auth);
        pushSubscriptionRepository.save(sub);
    }

    public static class SubscribeRequest {
        public String endpoint;
        public Keys keys;
        public static class Keys {
            public String p256dh;
            public String auth;
        }
    }
}
