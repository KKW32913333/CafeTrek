// Minimal service worker: makes CafeTrek installable as a PWA (spec's
// Safari iOS / Chrome Android / Chrome PC / PWA ホーム画面 targets) and
// handles incoming Web Push events from NotificationScheduler.

self.addEventListener("install", (event) => {
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  self.clients.claim();
});

self.addEventListener("push", (event) => {
  let data = { title: "CafeTrek", body: "新しい通知があります。" };
  try {
    data = event.data.json();
  } catch (e) {
    if (event.data) data.body = event.data.text();
  }
  event.waitUntil(
    self.registration.showNotification(data.title, {
      body: data.body,
      icon: "/icons/icon-192.png",
      badge: "/icons/icon-192.png",
    })
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  event.waitUntil(clients.openWindow("/"));
});
