# CafeTrek

「CafeTrekアプリ開発仕様書」のMVP範囲を、**Linkle（グループ共有システム）と同じ構成**で実装したものです。
Expo/React Nativeのネイティブアプリ構成から、Spring Boot + Thymeleafで描画する **PWA（Webアプリ）** に作り直しました。

## システム構成

```
開発者(MacBook/VSCode)
   │ git push
   ▼
GitHub (KKW32913333/CafeTrek)
   │ Webhook（自動デプロイ）
   ▼
Render — Dockerコンテナ / Spring Boot / port ${PORT}
   ├─ Spring Boot 3.x (Java 17)
   │    Controllers / Services / JPA Repositories
   ├─ Thymeleaf … HTMLテンプレートレンダリング
   ├─ Spring Security … セッション認証 / CSRF対策
   ├─ Scheduler … 毎朝8時「今日どこ行く？」通知
   └─ HikariCP … DBコネクションプール
        │ JDBC / SSL              │ HTTPS (Web Push API / Google Maps・Places API)
        ▼                          ▼
   Neon PostgreSQL           Web Push Service / Google Maps Platform
   (Serverless / 本番DB)          │ Push通知・地図・周辺カフェ検索
        │                          ▼
        └──────────────► クライアント（ブラウザ / PWA）
                          Safari(iOS) / Chrome(Android) / Chrome(PC) / PWA(ホーム画面)
```

## 実装済み機能

- **ホーム**：現在地から近いカフェ、お気に入りに追加したカフェを表示
- **地図**：Google Maps実連携（現在地・周辺カフェのピン表示）、キーワード検索、カテゴリフィルター（すべて／カフェ／スイーツ／作業向き）
- **カフェ詳細**：Google Places由来の店舗情報・写真・評価、公式サイトへのリンク（Place Details APIから取得、初回のみ）、Googleマップへのリンク
- **記録**：訪問日・コーヒー名・評価・味の特徴・雰囲気・感想を記録。あとから**編集・削除**が可能（マイページ→訪問履歴）
- **マイページ**：訪問数・お気に入り数・獲得バッジ、コーヒー図鑑（国別杯数）、訪問履歴一覧
- **認証**：メール/パスワードでのログイン・新規登録（Spring Security）。パスワードを忘れた場合は管理者への連絡を案内（自動リセットは未実装）
- **通知**：Web Push対応。毎朝8時に「今日どこ行く？」のリマインダー通知（VAPID鍵が設定されている場合のみ）
- **PWA**：ホーム画面に追加可能。アプリアイコン・サービスワーカー登録済み
- **パフォーマンス**：カフェ写真はブラウザに1日キャッシュ、同一地点でのPlaces API検索は10分間キャッシュ（無料枠の消費を抑制）

## ローカルで試す（H2、Neon・Google APIキー不要）

```bash
cd app
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

http://localhost:8080 を開くとログイン画面が出ます。

デモログイン: `coffeelover@example.com` / `password123`

Google Maps・Places・Web Pushの各キーを設定しない場合、地図とカフェ検索はダミー動作（何も表示されない/エラーにならず空で返る）になりますが、それ以外の機能（記録・お気に入り・マイページなど）は問題なく動作します。

## 環境変数一覧

| 変数名 | 用途 | 必須 |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Neon PostgreSQL接続情報 | 本番のみ必須 |
| `GOOGLE_MAPS_API_KEY` | 地図表示（ブラウザから直接呼ばれる、HTTPリファラー制限推奨） | 地図機能を使うなら必須 |
| `GOOGLE_PLACES_SERVER_API_KEY` | 周辺カフェ検索・写真・公式サイト取得（サーバーからのみ呼ばれる、制限なしキー） | 同上 |
| `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` / `VAPID_SUBJECT` | Web Push通知 | 通知機能を使うなら必須 |

## 本番デプロイ手順（Linkleと同じ流れ）

### 1. Neon PostgreSQLを用意する

1. https://neon.tech でプロジェクトを作成
2. ダッシュボードの接続文字列を確認（例: `postgresql://user:pass@ep-xxx.neon.tech/cafetrek?sslmode=require`）
3. 「Connection pooling」はオフ（HikariCPと二重プールになるのを避けるため）
4. これをJDBC形式に変換して環境変数に設定します
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech/cafetrek?sslmode=require&channelBinding=require`
   - `SPRING_DATASOURCE_USERNAME=user`
   - `SPRING_DATASOURCE_PASSWORD=pass`

### 2. Google Maps Platformを用意する

1. https://console.cloud.google.com でプロジェクトを作成し、お支払いを有効化
2. 「Maps JavaScript API」と「Places API」を有効化
3. **ブラウザ用キー**を発行し、HTTPリファラー制限（`https://cafetrek.onrender.com/*` 等）をかけて `GOOGLE_MAPS_API_KEY` に設定
4. **サーバー用キー**をもう1つ発行し、制限は「Places APIのみ」に絞って `GOOGLE_PLACES_SERVER_API_KEY` に設定（アプリケーションの制限は「なし」のまま。サーバーから呼ぶためリファラーが付かない）
5. Cloud Console →「割り当て」で日次リクエスト数に上限を設定しておくと安心です

### 3. VAPIDキーを発行する（Web Push用）

```bash
npx web-push generate-vapid-keys
```

出力された公開鍵・秘密鍵を `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` に設定し、`VAPID_SUBJECT` には `mailto:あなたのメールアドレス` を設定します。

### 4. GitHubにpushする

```bash
git add -A
git commit -m "..."
git push
```

### 5. Renderに接続する

1. https://render.com で「New +」→「Web Service」
2. GitHubリポジトリ（`KKW32913333/CafeTrek`）を選択
3. Environment: **Docker** を選択
4. **Root Directory** を `app` に設定（これを忘れると `Dockerfile: no such file or directory` エラーになります）
5. 上記の環境変数（`SPRING_DATASOURCE_*`, `GOOGLE_MAPS_API_KEY`, `GOOGLE_PLACES_SERVER_API_KEY`, `VAPID_*`）をすべて設定
6. 「Create Web Service」

これでGitHubにWebhookが設定され、以降 `git push` するたびに自動デプロイされます。GitHub Actions（`.github/workflows/ci.yml`）によるビルド確認も自動で走ります。

初回起動時、`data-postgresql.sql` によりNeon側にもデモユーザー・サンプルカフェが自動投入されます。

## ディレクトリ構成

```
app/
  src/main/java/com/cafetrek/
    controller/   PageController(画面・地図フィルター・訪問履歴の編集/削除),
                  AuthController(ログイン), PushSubscriptionController,
                  PlacePhotoController(写真プロキシ) など
    domain/       JPAエンティティ（User, Cafe, CafeVisit, Favorite, Coffee, Photo, PushSubscription）
    service/      CafeService, VisitService, FavoriteService, StatsService,
                  PlacesService(Google Places連携), WebPushService, NotificationScheduler
    config/       SecurityConfig, WebPushConfig, CorsConfig
  src/main/resources/
    templates/    Thymeleafテンプレート（home, map, cafe-detail, record, visits,
                  mypage, login, register）
    static/       CSS / JS / images(背景・代替写真) / manifest.json / service-worker.js（PWA）
    application.yml
  Dockerfile
render.yaml
.github/workflows/ci.yml   GitHub ActionsによるCI（push時に自動ビルド確認）
```

## 今後の拡張

- 写真アップロード（記録画面の「＋写真を追加」は現状未実装。無料枠のある外部ストレージ（Cloudinary等）との連携が必要）
- パスワードの自動リセット（現状は管理者へのメール連絡を案内するのみ。メール送信サービスとの連携が必要）
- 「今日どこ行く？」機能（気分に合わせたカフェ提案、仕様書 section 14）
- カフェ巡りルート、混雑度記録などPhase 2/3機能（仕様書 section 19, 20）
