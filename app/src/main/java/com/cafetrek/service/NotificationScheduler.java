package com.cafetrek.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 仕様書 section 14「今日どこ行く？」機能のリマインダー通知。
 * Linkleの「Scheduler: プッシュ通知 / 毎分・毎朝8時実行」と同じ役割。
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final WebPushService webPushService;

    /** Every day at 8:00 JST. */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Tokyo")
    public void sendDailyReminder() {
        webPushService.sendToAll(
                "CafeTrek",
                "今日はどんなカフェに行きたいですか？☕ 気になるカフェを探してみましょう。"
        );
    }
}
