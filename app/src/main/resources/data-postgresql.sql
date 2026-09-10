-- Seed data for Neon/Postgres. Safe to re-run (ON CONFLICT DO NOTHING).
-- Demo login: coffeelover@example.com / password123

INSERT INTO users (id, name, email, password, role, created_at, updated_at)
VALUES (1, 'CoffeeLover', 'coffeelover@example.com',
        '$2b$10$P3fck4c.ZrhNSXgrcO/n3eaxHn/twm1H0J3uEthCr76.JOGkcHYj2', 'USER', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO cafes (id, google_place_id, name, address, latitude, longitude, rating, created_at, updated_at) VALUES
  (1, 'demo_onibus',    'ONIBUS COFFEE',           '東京都渋谷区上原1-19-8',       35.6635, 139.6789, 4.5, now(), now()),
  (2, 'demo_littlenap', 'Little Nap Coffee Stand',  '東京都渋谷区代々木5-65-4',     35.6702, 139.6897, 4.2, now(), now()),
  (3, 'demo_rec',       'REC COFFEE',               '福岡県福岡市中央区警固1-2-14', 33.5875, 130.3900, 4.6, now(), now()),
  (4, 'demo_streamer',  'STREAMER COFFEE',          '東京都渋谷区神南1-19-8',       35.6618, 139.6982, 4.3, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO coffees (id, name, country, region, roast_level, processing_method) VALUES
  (1, 'イルガチェフェ', 'エチオピア', 'イルガチェフェ', '浅煎り', 'ウォッシュド'),
  (2, 'セラード',        'ブラジル',   'セラード',       '中煎り', 'ナチュラル'),
  (3, 'ウイラ',           'コロンビア', 'ウイラ',         '中煎り', 'ウォッシュド'),
  (4, 'アンティグア',    'グアテマラ', 'アンティグア',   '中深煎り', 'ウォッシュド')
ON CONFLICT (id) DO NOTHING;

INSERT INTO favorites (id, user_id, cafe_id, created_at) VALUES
  (1, 1, 1, now()),
  (2, 1, 3, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO cafe_visits
  (id, user_id, cafe_id, coffee_id, visited_at, rating, price, acidity, bitterness, sweetness, body, fruity, nutty, atmosphere, comment, created_at, updated_at)
VALUES
  (1, 1, 1, 1, '2026-08-20', 4, 600, TRUE, FALSE, FALSE, FALSE, TRUE, FALSE, '静か',
   '酸味が強くてフルーティー。店内も静かでゆっくりできました。', now(), now()),
  (2, 1, 3, 2, '2026-08-25', 5, 550, FALSE, TRUE, TRUE, TRUE, FALSE, FALSE, '作業向き',
   'コクがあって満足度が高い。Wi-Fiも快適で作業がはかどりました。', now(), now())
ON CONFLICT (id) DO NOTHING;

-- Keep the id sequences ahead of the seeded rows so new inserts don't collide.
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('cafes', 'id'), (SELECT MAX(id) FROM cafes));
SELECT setval(pg_get_serial_sequence('coffees', 'id'), (SELECT MAX(id) FROM coffees));
SELECT setval(pg_get_serial_sequence('favorites', 'id'), (SELECT MAX(id) FROM favorites));
SELECT setval(pg_get_serial_sequence('cafe_visits', 'id'), (SELECT MAX(id) FROM cafe_visits));
