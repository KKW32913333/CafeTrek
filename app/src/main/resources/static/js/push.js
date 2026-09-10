// Registers the service worker and subscribes the browser to Web Push,
// posting the subscription to /api/push/subscribe. Called from a button
// on マイページ ("通知をオンにする"), since Push requires a user gesture
// and explicit permission on both iOS Safari and Chrome.

function urlBase64ToUint8Array(base64String) {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = atob(base64);
  return Uint8Array.from([...rawData].map((c) => c.charCodeAt(0)));
}

async function enablePushNotifications() {
  if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
    alert("お使いのブラウザはプッシュ通知に対応していません。");
    return;
  }

  const reg = await navigator.serviceWorker.register("/service-worker.js");

  const permission = await Notification.requestPermission();
  if (permission !== "granted") {
    alert("通知が許可されませんでした。ブラウザの設定から許可できます。");
    return;
  }

  const { publicKey } = await fetch("/api/push/public-key").then((r) => r.json());
  if (!publicKey) {
    alert("サーバー側でVAPIDキーが未設定です（README参照）。");
    return;
  }

  const subscription = await reg.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: urlBase64ToUint8Array(publicKey),
  });

  await fetch("/api/push/subscribe", {
    method: "POST",
    headers: csrfHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(subscription),
  });

  alert("通知をオンにしました。毎朝8時に「今日どこ行く？」の通知が届きます。");
}

if ("serviceWorker" in navigator) {
  navigator.serviceWorker.register("/service-worker.js").catch(() => {});
}
