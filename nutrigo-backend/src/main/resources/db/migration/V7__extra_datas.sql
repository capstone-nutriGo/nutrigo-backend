-- 🔥 DEV / TEST 전용 초기화 스크립트

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE meal_log;
TRUNCATE TABLE daily_intake_summary;
TRUNCATE TABLE user_challenge;
TRUNCATE TABLE challenge;
TRUNCATE TABLE user_setting;
TRUNCATE TABLE user;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user (
    user_id, email, nickname, name, gender, birthday, password
) VALUES
      (1, 'anna@example.com',      'anna',      '안나',      'female', '2003-09-26', 'encoded_pw_1'),
      (2, 'jihu@example.com',      'jihu',      '지후',      'male',   '2003-01-15', 'encoded_pw_2'),
      (3, 'jaehoon@example.com',   'jaehoon',   '재훈',      'male',   '2002-12-01', 'encoded_pw_3'),
      (4, 'soyeon@example.com',    'soyeon',    '소연',      'female', '2004-03-10', 'encoded_pw_4'),
      (5, 'minji@example.com',     'minji',     '민지',      'female', '2003-07-22', 'encoded_pw_5'),
      (6, 'tester@example.com',    'tester',    '테스터',    'other',  '1999-05-05', 'encoded_pw_6');
INSERT INTO user_setting (user_id) VALUES
                                       (1),
                                       (2),
                                       (3),
                                       (4),
                                       (5),
                                       (6);
INSERT INTO challenge (
    challenge_id, code, title, type, duration_days, created_by
) VALUES
      (1,  'KCAL_7D_1800',      '7일 동안 1800kcal 이하 식단',         'kcal',      7,  1),
      (2,  'KCAL_14D_2000',     '2주 동안 2000kcal 이하 유지',         'kcal',      14, 1),
      (3,  'SODIUM_7D_2000',    '7일 동안 나트륨 2000mg 이하',         'sodium',    7,  2),
      (4,  'SODIUM_30D_2500',   '한 달간 나트륨 2500mg 이하',          'sodium',    30, 2),
      (5,  'PROTEIN_7D_60',     '일주일 동안 단백질 60g 이상 섭취',     'protein',   7,  3),
      (6,  'PROTEIN_30D_80',    '30일간 단백질 80g 이상 섭취',         'protein',   30, 3),
      (7,  'FREQ_7D_2MEAL',     '7일 동안 하루 2끼 이상 기록',         'frequency', 7,  4),
      (8,  'FREQ_14D_3MEAL',    '2주 동안 하루 3끼 식사 기록',         'frequency', 14, 4),
      (9,  'DAYCOLOR_7D',       '7일 연속 식단 색깔 기록 도전',        'day_color', 7,  5),
      (10, 'DAYCOLOR_21D',      '21일 동안 꾸준한 컬러풀 식단',        'day_color', 21, 5);
INSERT INTO user_challenge (
    user_challenge_id, started_at, user_id, challenge_id
) VALUES
      -- Anna
      (1,  '2025-12-01', 1, 1),
      (2,  '2025-12-08', 1, 3),
      (3,  '2025-11-20', 1, 9),

      -- Jihu
      (4,  '2025-12-01', 2, 2),
      (5,  '2025-11-15', 2, 4),
      (6,  '2025-11-25', 2, 7),

      -- Jaehoon
      (7,  '2025-11-28', 3, 5),
      (8,  '2025-12-05', 3, 6),

      -- Soyeon
      (9,  '2025-12-01', 4, 7),
      (10, '2025-12-03', 4, 8),

      -- Minji
      (11, '2025-11-30', 5, 1),
      (12, '2025-12-02', 5, 10),

      -- Tester
      (13, '2025-11-10', 6, 2),
      (14, '2025-11-20', 6, 3),
      (15, '2025-11-25', 6, 5),
      (16, '2025-11-28', 6, 8),
      (17, '2025-12-01', 6, 9),
      (18, '2025-12-05', 6, 10);

INSERT INTO daily_intake_summary (
    daily_intake_summary_id, user_id, date
) VALUES
      -- Anna (1) : 2025-12-01 ~ 2025-12-06
      (1,  1, '2025-12-01'),
      (2,  1, '2025-12-02'),
      (3,  1, '2025-12-03'),
      (4,  1, '2025-12-04'),
      (5,  1, '2025-12-05'),
      (6,  1, '2025-12-06'),

      -- Jihu (2) : 2025-11-28 ~ 2025-12-03
      (7,  2, '2025-11-28'),
      (8,  2, '2025-11-29'),
      (9,  2, '2025-11-30'),
      (10, 2, '2025-12-01'),
      (11, 2, '2025-12-02'),
      (12, 2, '2025-12-03'),

      -- Jaehoon (3) : 2025-12-01 ~ 2025-12-04
      (13, 3, '2025-12-01'),
      (14, 3, '2025-12-02'),
      (15, 3, '2025-12-03'),
      (16, 3, '2025-12-04'),

      -- Soyeon (4) : 2025-12-01 ~ 2025-12-03
      (17, 4, '2025-12-01'),
      (18, 4, '2025-12-02'),
      (19, 4, '2025-12-03'),

      -- Minji (5) : 2025-11-29 ~ 2025-12-02
      (20, 5, '2025-11-29'),
      (21, 5, '2025-11-30'),
      (22, 5, '2025-12-01'),
      (23, 5, '2025-12-02'),

      -- Tester (6) : 2025-11-25 ~ 2025-11-30
      (24, 6, '2025-11-25'),
      (25, 6, '2025-11-26'),
      (26, 6, '2025-11-27'),
      (27, 6, '2025-11-28'),
      (28, 6, '2025-11-29'),
      (29, 6, '2025-11-30');
INSERT INTO meal_log (
    meal_log_id, daily_intake_summary_id, created_at
) VALUES
      -- Anna (summary_id: 1~6), 하루 3끼씩
      (1,  1, '2025-12-01 08:15:00'),
      (2,  1, '2025-12-01 12:32:00'),
      (3,  1, '2025-12-01 19:45:00'),

      (4,  2, '2025-12-02 08:10:00'),
      (5,  2, '2025-12-02 12:40:00'),
      (6,  2, '2025-12-02 21:05:00'),

      (7,  3, '2025-12-03 09:00:00'),
      (8,  3, '2025-12-03 13:10:00'),
      (9,  3, '2025-12-03 20:30:00'),

      (10, 4, '2025-12-04 08:05:00'),
      (11, 4, '2025-12-04 12:20:00'),
      (12, 4, '2025-12-04 19:50:00'),

      (13, 5, '2025-12-05 08:30:00'),
      (14, 5, '2025-12-05 13:05:00'),
      (15, 5, '2025-12-05 20:10:00'),

      (16, 6, '2025-12-06 09:15:00'),
      (17, 6, '2025-12-06 12:50:00'),
      (18, 6, '2025-12-06 19:30:00'),

      -- Jihu (summary_id: 7~12)
      (19, 7, '2025-11-28 09:10:00'),
      (20, 7, '2025-11-28 13:20:00'),
      (21, 7, '2025-11-28 21:00:00'),

      (22, 8, '2025-11-29 08:50:00'),
      (23, 8, '2025-11-29 19:40:00'),

      (24, 9, '2025-11-30 10:20:00'),
      (25, 9, '2025-11-30 14:10:00'),
      (26, 9, '2025-11-30 20:55:00'),

      (27, 10, '2025-12-01 09:05:00'),
      (28, 10, '2025-12-01 12:35:00'),
      (29, 10, '2025-12-01 18:10:00'),

      (30, 11, '2025-12-02 08:25:00'),
      (31, 11, '2025-12-02 13:15:00'),

      (32, 12, '2025-12-03 09:00:00'),
      (33, 12, '2025-12-03 12:45:00'),
      (34, 12, '2025-12-03 20:15:00'),

      -- Jaehoon (summary_id: 13~16)
      (35, 13, '2025-12-01 07:50:00'),
      (36, 13, '2025-12-01 12:10:00'),
      (37, 13, '2025-12-01 18:40:00'),

      (38, 14, '2025-12-02 08:05:00'),
      (39, 14, '2025-12-02 13:00:00'),
      (40, 14, '2025-12-02 19:20:00'),

      (41, 15, '2025-12-03 08:45:00'),
      (42, 15, '2025-12-03 12:40:00'),
      (43, 15, '2025-12-03 21:10:00'),

      (44, 16, '2025-12-04 09:30:00'),
      (45, 16, '2025-12-04 13:20:00'),
      (46, 16, '2025-12-04 20:05:00'),

      -- Soyeon (summary_id: 17~19)
      (47, 17, '2025-12-01 08:00:00'),
      (48, 17, '2025-12-01 12:15:00'),
      (49, 17, '2025-12-01 19:00:00'),

      (50, 18, '2025-12-02 09:10:00'),
      (51, 18, '2025-12-02 13:25:00'),
      (52, 18, '2025-12-02 18:50:00'),

      (53, 19, '2025-12-03 08:55:00'),
      (54, 19, '2025-12-03 12:35:00'),
      (55, 19, '2025-12-03 21:20:00'),

      -- Minji (summary_id: 20~23)
      (56, 20, '2025-11-29 09:05:00'),
      (57, 20, '2025-11-29 12:40:00'),
      (58, 20, '2025-11-29 19:05:00'),

      (59, 21, '2025-11-30 08:50:00'),
      (60, 21, '2025-11-30 13:05:00'),
      (61, 21, '2025-11-30 20:00:00'),

      (62, 22, '2025-12-01 09:20:00'),
      (63, 22, '2025-12-01 12:55:00'),
      (64, 22, '2025-12-01 18:30:00'),

      (65, 23, '2025-12-02 08:35:00'),
      (66, 23, '2025-12-02 13:10:00'),
      (67, 23, '2025-12-02 19:40:00'),

      -- Tester (summary_id: 24~29)
      (68, 24, '2025-11-25 08:10:00'),
      (69, 24, '2025-11-25 12:20:00'),
      (70, 24, '2025-11-25 19:15:00'),

      (71, 25, '2025-11-26 09:00:00'),
      (72, 25, '2025-11-26 13:00:00'),
      (73, 25, '2025-11-26 20:10:00'),

      (74, 26, '2025-11-27 08:45:00'),
      (75, 26, '2025-11-27 12:35:00'),
      (76, 26, '2025-11-27 18:55:00'),

      (77, 27, '2025-11-28 09:15:00'),
      (78, 27, '2025-11-28 13:30:00'),
      (79, 27, '2025-11-28 20:20:00'),

      (80, 28, '2025-11-29 08:30:00'),
      (81, 28, '2025-11-29 12:50:00'),
      (82, 28, '2025-11-29 19:30:00'),

      (83, 29, '2025-11-30 09:05:00'),
      (84, 29, '2025-11-30 13:20:00'),
      (85, 29, '2025-11-30 21:00:00');
UPDATE challenge c
SET
    -- 설명 없으면 "타이틀 + 기본 설명"
    c.description = COALESCE(
            c.description,
            CONCAT(c.title, ' 챌린지에 대한 기본 설명입니다.')
                    ),

    -- status NULL이면 ACTIVE
    c.status = COALESCE(c.status, 'ACTIVE'),

    -- type에 따라 category 기본값 HEALTH/FUN
    c.category = COALESCE(
            c.category,
            CASE c.type
                WHEN 'kcal'    THEN 'HEALTH'
                WHEN 'sodium'  THEN 'HEALTH'
                WHEN 'protein' THEN 'HEALTH'
                ELSE 'FUN'
                END
                 ),

    -- 커스텀 챌린지 여부: 기본은 시스템 챌린지
    c.is_custom = COALESCE(c.is_custom, 0),

    -- 만든 사람 없으면 1번 유저 기준
    c.created_by = COALESCE(c.created_by, 1),

    -- 목표 횟수: 타입에 따라 대략적인 기본값
    c.target_count = COALESCE(
            c.target_count,
            CASE c.type
                WHEN 'frequency' THEN 14
                WHEN 'day_color' THEN 7
                ELSE 10
                END
                     ),

    -- 한 끼 최대 칼로리: 500~900 랜덤
    c.max_kcal_per_meal = COALESCE(
            c.max_kcal_per_meal,
            FLOOR(500 + RAND() * 400)
                          ),

    -- 한 끼 최대 나트륨: 800~2000mg 랜덤
    c.max_sodium_mg_per_meal = COALESCE(
            c.max_sodium_mg_per_meal,
            FLOOR(800 + RAND() * 1200)
                               ),

    -- 커스텀 설명은 기본 빈 문자열
    c.custom_description = COALESCE(c.custom_description, ''),

    -- 수정 시간 없으면 지금으로
    c.updated_at = COALESCE(c.updated_at, NOW());
UPDATE daily_intake_summary d
SET
    d.total_kcal = COALESCE(
            d.total_kcal,
            ROUND(1200 + RAND() * 1300, 1)   -- 1200 ~ 2500
                   ),
    d.total_sodium_mg = COALESCE(
            d.total_sodium_mg,
            ROUND(500 + RAND() * 3500, 1)    -- 500 ~ 4000
                        ),
    d.total_protein_g = COALESCE(
            d.total_protein_g,
            ROUND(30 + RAND() * 90, 1)       -- 30 ~ 120
                        ),
    d.total_carb_g = COALESCE(
            d.total_carb_g,
            ROUND(100 + RAND() * 300, 1)     -- 100 ~ 400
                     ),
    d.total_snack = COALESCE(
            d.total_snack,
            FLOOR(RAND() * 4)                -- 0 ~ 3
                    ),
    d.total_night = COALESCE(
            d.total_night,
            FLOOR(RAND() * 3)                -- 0 ~ 2
                    ),
    d.day_score = COALESCE(
            d.day_score,
            ROUND(RAND() * 100, 1)           -- 0 ~ 100
                  );
UPDATE meal_log m
SET
    -- 메뉴 이름이 없으면 랜덤으로 부여
    m.menu = COALESCE(
            m.menu,
            CASE FLOOR(RAND() * 5)
                WHEN 0 THEN '닭가슴살 샐러드'
                WHEN 1 THEN '현미밥 정식'
                WHEN 2 THEN '비빔밥'
                WHEN 3 THEN '파스타'
                ELSE '치킨 세트'
                END
             ),

    -- 카테고리 없으면 랜덤으로 부여
    m.category = COALESCE(
            m.category,
            CASE FLOOR(RAND() * 4)
                WHEN 0 THEN '한식'
                WHEN 1 THEN '양식'
                WHEN 2 THEN '분식'
                ELSE '패스트푸드'
                END
                 ),

    -- 한 끼 칼로리 200~1000
    m.kcal = COALESCE(
            m.kcal,
            ROUND(200 + RAND() * 800, 1)       -- 200 ~ 1000
             ),

    -- 나트륨 200~2500mg
    m.sodium_mg = COALESCE(
            m.sodium_mg,
            ROUND(200 + RAND() * 2300, 1)      -- 200 ~ 2500
                  ),

    -- 단백질 5~60g
    m.protein_g = COALESCE(
            m.protein_g,
            ROUND(5 + RAND() * 55, 1)          -- 5 ~ 60
                  ),

    -- 탄수화물 10~150g
    m.carb_g = COALESCE(
            m.carb_g,
            ROUND(10 + RAND() * 140, 1)        -- 10 ~ 150
               ),

    -- 이 끼니의 점수 0~100
    m.total_score = COALESCE(
            m.total_score,
            ROUND(RAND() * 100, 1)
                    ),

    -- created_at 시간대 기준으로 식사 시간 분류
    m.meal_time = COALESCE(
            m.meal_time,
            CASE
                WHEN HOUR(m.created_at) BETWEEN 5  AND 10 THEN 'BREAKFAST'
                WHEN HOUR(m.created_at) BETWEEN 11 AND 15 THEN 'LUNCH'
                WHEN HOUR(m.created_at) BETWEEN 16 AND 21 THEN 'DINNER'
                WHEN HOUR(m.created_at) BETWEEN 22 AND 23 THEN 'NIGHT'
                ELSE 'SNACK'
                END
                  ),

    -- meal_date 없으면 created_at 날짜 사용
    m.meal_date = COALESCE(
            m.meal_date,
            DATE(m.created_at)
                  );
UPDATE user_challenge uc
    JOIN challenge c ON uc.challenge_id = c.challenge_id
SET
    uc.ended_at = COALESCE(
            uc.ended_at,
            DATE_ADD(uc.started_at, INTERVAL c.duration_days DAY)
                  ),
    uc.finished_at = COALESCE(
            uc.finished_at,
            DATE_ADD(uc.started_at, INTERVAL c.duration_days DAY)
                     ),
    uc.progress_rate = COALESCE(
            uc.progress_rate,
            100
                       );
