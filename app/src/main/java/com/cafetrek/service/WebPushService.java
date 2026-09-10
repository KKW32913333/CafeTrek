package com.cafetrek.service;

import com.cafetrek.domain.PushSubscription;
import com.cafetrek.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private final PushService pushService;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${webpush.public-key}")
    private String publicKey;

    public boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank();
    }

    /** Sends `body` to every subscription the given user has registered. */
    public void sendToUser(Long userId, String title, String body) {
        sendToSubscriptions(pushSubscriptionRepository.findByUserId(userId), title, body);
    }

    /** Sends `body` to every subscribed device across all users — used by the daily reminder. */
    public void sendToAll(String title, String body) {
        sendToSubscriptions(pushSubscriptionRepository.findAll(), title, body);
    }

    private void sendToSubscriptions(List<PushSubscription> subs, String title, String body) {
        if (!isConfigured()) {
            log.warn("VAPID keys not configured (webpush.public-key / private-key) — skipping {} push(es).", subs.size());
            return;
        }
        String payload = "{\"title\":\"" + escape(title) + "\",\"body\":\"" + escape(body) + "\"}";

        for (PushSubscription sub : subs) {
            try {
                nl.martijndwars.webpush.Subscription subscription = new nl.martijndwars.webpush.Subscription(
                        sub.getEndpoint(),
                        new nl.martijndwars.webpush.Subscription.Keys(sub.getP256dh(), sub.getAuth()));
                Notification notification = new Notification(subscription, payload);
                pushService.send(notification);
            } catch (Exception e) {
                log.warn("Push send failed for endpoint {}: {}", sub.getEndpoint(), e.getMessage());
            }
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
