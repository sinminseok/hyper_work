
-- 테스트용 데이터를 삽입합니다. (NOW()는 2025-08-11을 기준으로 계산됩니다)
INSERT INTO game (
    name, type, distance, game_date, start_at, end_at, status,
    created_at, modified_at, participated_count, total_prize,
    first_place_prize, second_place_prize, third_place_prize,
    first_user_name, second_user_name, third_user_name
) VALUES
-- ====================================================================================================
-- 1. 완료(FINISHED)된 게임들 (페이지 첫 로딩 시 이 데이터들이 보여야 합니다)
-- ====================================================================================================
-- ID 1: 2일 전에 생성된 완료된 게임 (기본 검색 범위에 포함됨)
('주말 마무리 달리기', 'SPEED', 'TEN_KM_COURSE', DATE_SUB(CURDATE(), INTERVAL 2 DAY),
 DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 8 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 10 HOUR), 'FINISHED',
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 155, 150000, 120000, 20000, 10000, 'Winner1', 'RunnerUp1', 'ThirdPlace1'),

-- ID 2: 5일 전에 생성된 완료된 게임 (기본 검색 범위에 포함됨)
('지난 주 수요일 챌린지', 'HEARTBEAT', 'FIVE_KM_COURSE', DATE_SUB(CURDATE(), INTERVAL 5 DAY),
 DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 19 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 20 HOUR), 'FINISHED',
 DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 210, 100000, 80000, 15000, 5000, 'Winner2', 'RunnerUp2', NULL),

-- ID 3: 10일 전에 생성된 완료된 게임 (기본 검색 범위를 벗어남)
('오래된 하프 마라톤', 'CADENCE', 'HALF_COURSE', DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 10 DAY), INTERVAL 7 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 10 DAY), INTERVAL 11 HOUR), 'FINISHED',
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 95, 200000, 150000, 40000, 10000, 'Winner3', 'RunnerUp3', 'ThirdPlace3'),

-- ====================================================================================================
-- 2. 예정(SCHEDULED)된 게임들
-- ====================================================================================================
-- ID 4: 오늘 생성된, 내일 시작될 게임
('내일 새벽 공복 유산소', 'FLIGHT_TIME', 'FIVE_KM_COURSE', DATE_ADD(CURDATE(), INTERVAL 1 DAY),
 DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 6 HOUR), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 7 HOUR), 'SCHEDULED',
 NOW(), NOW(), 5, 80000, 60000, 15000, 5000, NULL, NULL, NULL),

-- ID 5: 어제 생성된, 다음 주에 시작될 게임
('다음 주 주말 풀코스', 'VERTICAL_OSCILLATION', 'FULL_COURSE', DATE_ADD(CURDATE(), INTERVAL 7 DAY),
 DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 7 DAY), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 7 DAY), INTERVAL 13 HOUR), 'SCHEDULED',
 DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 2, 300000, 250000, 40000, 10000, NULL, NULL, NULL),

-- ====================================================================================================
-- 3. 진행 중(PROGRESS)인 게임
-- ====================================================================================================
-- ID 6: 오늘 생성된, 현재 진행 중인 게임
('오늘의 점심 번개런', 'POWER', 'FIVE_KM_COURSE', CURDATE(),
 DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'PROGRESS',
 NOW(), NOW(), 55, 90000, 70000, 15000, 5000, NULL, NULL, NULL),

-- ====================================================================================================
-- 4. 취소(CANCELED)된 게임
-- ====================================================================================================
-- ID 7: 3일 전에 생성된, 어제의 취소된 게임
('우천 취소된 어제의 게임', 'SPEED', 'HALF_COURSE', DATE_SUB(CURDATE(), INTERVAL 1 DAY),
 DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 18 HOUR), 'CANCELED',
 DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 150, 180000, 140000, 30000, 10000, NULL, NULL, NULL);
