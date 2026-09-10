# CafeTrek

「CafeTrekアプリ開発仕様書」のMVP範囲を、**Linkle（グループ共有システム）と同じ構成**で実装したものです。
Expo/React Nativeのネイティブアプリ構成から、Spring Boot + Thymeleafで描画する **PWA（Webアプリ）** に作り直しました。

## システム構成

```
開発者(MacBook/VSCode)
   │ git push
   ▼
GitHub (このリポジトリ)
   │ Webhook（自動デプロイ）
   ▼
Render — Dockerコンテナ / Spring Boot / port ${PORT}
   ├─ Spring Boot 3.x (Java 17)
   │    Controllers / Services / JPA Repositories
   ├─ Thymeleaf … HTMLテンプレートレンダリング
   ├─ Spring Security … セッション認証 / CSRF対策
   ├─ Scheduler … 毎朝8時「今日どこ行く？」通知
   └─ HikariCP … DBコネクションプール
        │ JDBC / SSL              │ HTTPS (Web Push API)
        ▼                          ▼
   Neon PostgreSQL           Web Push Service (FCM / APNs, VAPID認証)
   (Serverless / 本番DB)          │ Push通知
        │                          ▼
        └──────────────► クライアント（ブラウザ / PWA）
                          Safari(iOS) / Chrome(Android) / Chrome(PC) / PWA(ホーム画面)
```

Linkleの図との対応：

| Linkle | CafeTrek |
|---|---|
| GitHub: `KKW32913333/schedule-app` | GitHub: あなたが作成する新規リポジトリ |
| Render — port 10000 | Render — `server.port: ${PORT:10000}` |
| Neon PostgreSQL（14テーブル） | Neon PostgreSQL（users / cafes / coffees / cafe_visits / favorites / photos / push_subscriptions の7テーブル） |
| Scheduler（毎分・毎朝8時実行） | Scheduler（毎朝8時「今日どこ行く？」通知） |
| Web Push Service（FCM/APNs, VAPID） | 同左（`nl.martijndwars:web-push`） |

## ローカルで試す（H2、Neonアカウント不要）

```bash
cd app
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

http://localhost:8080 を開くとログイン画面が出ます。

デモログイン: `coffeelover@example.com` / `password123`

## 本番デプロイ手順（Linkleと同じ流れ）

### 1. Neon PostgreSQLを用意する

1. https://neon.tech でプロジェクトを作成
2. ダッシュボードの接続文字列を確認（例: `postgresql://user:pass@ep-xxx.neon.tech/cafetrek?sslmode=require`）
3. これをJDBC形式に変換して環境変数に設定します
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech/cafetrek?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME=user`
   - `SPRING_DATASOURCE_PASSWORD=pass`

### 2. VAPIDキーを発行する（Web Push用）

```bash
npx web-push generate-vapid-keys
```

出力された公開鍵・秘密鍵を環境変数に設定します。

- `VAPID_PUBLIC_KEY`
- `VAPID_PRIVATE_KEY`
- `VAPID_SUBJECT=mailto:あなたのメールアドレス`

### 3. GitHubにpushする

前回ご案内した通り、新規リポジトリを作成してこのプロジェクト一式をpushしてください。

### 4. Renderに接続する

1. https://render.com で「New +」→「Web Service」
2. GitHubリポジトリを選択（初回はRenderにGitHubアクセスを許可）
3. Environment: **Docker** を選択
4. **Root Directory** を `app` に設定（これを忘れると `Dockerfile: no such file or directory` エラーになります。リポジトリのルートに`app/`と`README.md`が並んでいて、Dockerfileは`app/`の中にあるためです）
5. 上記の環境変数（`SPRING_DATASOURCE_*`, `VAPID_*`）を設定
6. 「Create Web Service」

これでGitHubにWebhookが設定され、以降 `git push` するたびに自動デプロイされます（Linkleと同じ）。

初回起動時、`data-postgresql.sql` によりNeon側にもデモユーザー・サンプルカフェが自動投入されます。

## ディレクトリ構成

```
app/
  src/main/java/com/cafetrek/
    controller/   PageController(画面), AuthController(ログイン), PushSubscriptionController など
    domain/       JPAエンティティ（User, Cafe, CafeVisit, Favorite, Coffee, Photo, PushSubscription）
    service/      CafeService, VisitService, FavoriteService, StatsService, WebPushService, NotificationScheduler
    config/       SecurityConfig, WebPushConfig, CorsConfig
  src/main/resources/
    templates/    Thymeleafテンプレート（home, map, cafe-detail, record, mypage, login, register）
    static/       CSS / JS / manifest.json / service-worker.js（PWA）
    application.yml
  Dockerfile
  render.yaml
```

## 今後の拡張

- Google Maps実接続（現状は地図画面ダミー表示。APIキー取得後、`/map`コントローラとテンプレートにPlaces API連携を追加）
- 写真アップロード（現状UI未実装。S3/Firebase Storage等と連携する`PhotoController`を追加）
- カフェ巡りルート、混雑度記録などPhase 2/3機能（仕様書 section 19, 20）
