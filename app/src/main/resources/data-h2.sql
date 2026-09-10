-- Seed data for the local H2 profile. Demo login: coffeelover@example.com / password123

MERGE INTO users (id, name, email, password, role, created_at, updated_at) KEY (id)
VALUES (1, 'CoffeeLover', 'coffeelover@example.com',
        '$2b$10$P3fck4c.ZrhNSXgrcO/n3eaxHn/twm1H0J3uEthCr76.JOGkcHYj2', 'USER', NOW(), NOW());

MERGE INTO cafes (id, google_place_id, name, address, latitude, longitude, rating, created_at, updated_at) KEY (id) VALUES
  (1, 'demo_onibus',    'ONIBUS COFFEE',           '東京都渋谷区上原1-19-8',       35.6635, 139.6789, 4.5, NOW(), NOW()),
  (2, 'demo_littlenap', 'Little Nap Coffee Stand',  '東京都渋谷区代々木5-65-4',     35.6702, 139.6897, 4.2, NOW(), NOW()),
  (3, 'demo_rec',       'REC COFFEE',               '福岡県福岡市中央区警固1-2-14', 33.5875, 130.3900, 4.6, NOW(), NOW()),
  (4, 'demo_streamer',  'STREAMER COFFEE',          '東京都渋谷区神南1-19-8',       35.6618, 139.6982, 4.3, NOW(), NOW());

MERGE INTO coffees (id, name, country, region, roast_level, processing_method) KEY (id) VALUES
  (1, 'イルガチェフェ', 'エチオピア', 'イルガチェフェ', '浅煎り', 'ウォッシュド'),
  (2, 'セラード',        'ブラジル',   'セラード',       '中煎り', 'ナチュラル'),
  (3, 'ウイラ',           'コロンビア', 'ウイラ',         '中煎り', 'ウォッシュド'),
  (4, 'アンティグア',    'グアテマラ', 'アンティグア',   '中深煎り', 'ウォッシュド');

MERGE INTO favorites (id, user_id, cafe_id, created_at) KEY (id) VALUES
  (1, 1, 1, NOW()),
  (2, 1, 3, NOW());

MERGE INTO cafe_visits
  (id, user_id, cafe_id, coffee_id, visited_at, rating, price, acidity, bitterness, sweetness, body, fruity, nutty, atmosphere, comment, created_at, updated_at)
KEY (id) VALUES
  (1, 1, 1, 1, '2026-08-20', 4, 600, TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, '静か',
   '酸味が強くてフルーティー。店内も静かでゆっくりできました。', NOW(), NOW()),
  (2, 1, 3, 2, '2026-08-25', 5, 550, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, '作業向き',
   'コクがあって満足度が高い。Wi-Fiも快適で作業がはかどりました。', NOW(), NOW());
