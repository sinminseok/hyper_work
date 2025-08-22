INSERT INTO game (name, type, status, admin_status, distance, game_date, start_at, end_at, participated_count, total_prize, first_place_prize, second_place_prize, third_place_prize, first_user_name, second_user_name, third_user_name, create_date_time, modified_date_time) VALUES
('제1회 서울 달빛 마라톤', 'SPEED', 'PARTICIPATE_FINISH', 'FINISHED', 'TEN_KM_COURSE', '2025-07-20', '2025-07-20 20:00:00', '2025-07-20 22:00:00', 150, 180000.0, 144000.0, 27000.0, 9000.0, '김민준', '이서준', '박하준', '2025-06-01 10:00:00', '2025-07-21 09:30:00'),
('가을맞이 남산 챌린지', 'HEARTBEAT', 'REGISTRATION_OPEN', 'SCHEDULED', 'FIVE_KM_COURSE', '2025-09-15', '2025-09-15 09:00:00', '2025-09-15 10:00:00', 85, 102000.0, 81600.0, 15300.0, 5100.0, NULL, NULL, NULL, '2025-08-05 14:00:00', '2025-08-05 14:05:00'),
('2025 하이퍼런 하프 코스', 'CADENCE', 'REGISTRATION_OPEN', 'SCHEDULED', 'HALF_COURSE', '2025-10-05', '2025-10-05 08:00:00', '2025-10-05 11:00:00', 220, 264000.0, 211200.0, 39600.0, 13200.0, NULL, NULL, NULL, '2025-07-15 11:20:00', '2025-08-10 18:00:00'),
('혹한기 극복! 파워 레이스', 'POWER', 'PARTICIPATE_FINISH', 'FINISHED', 'FIVE_KM_COURSE', '2025-01-12', '2025-01-12 10:00:00', '2025-01-12 11:00:00', 115, 138000.0, 110400.0, 20700.0, 6900.0, '최지아', '박서연', '정은우', '2024-11-20 09:00:00', '2025-01-13 11:00:00'),
('한강 나이트워크 10K', 'SPEED', 'PARTICIPATE_FINISH', 'FINISHED', 'TEN_KM_COURSE', '2025-08-21', '2025-08-21 12:00:00', '2025-08-21 14:00:00', 98, 117600.0, 94080.0, 17640.0, 5880.0, '송하영', '강지민', '윤태오', '2025-07-22 16:00:00', '2025-08-22 01:00:00'),
('부산 바다 풀코스 마라톤', 'VERTICAL_OSCILLATION', 'REGISTRATION_COMPLETE', 'SCHEDULED', 'FULL_COURSE', '2025-11-23', '2025-11-23 07:00:00', '2025-11-23 12:00:00', 450, 540000.0, 432000.0, 81000.0, 27000.0, NULL, NULL, NULL, '2025-05-10 13:00:00', '2025-08-20 10:00:00'),
('새해맞이 일출 러닝', 'GROUND_CONTACT_TIME', 'PARTICIPATE_FINISH', 'FINISHED', 'FIVE_KM_COURSE', '2025-01-01', '2025-01-01 07:00:00', '2025-01-01 08:00:00', 180, 216000.0, 172800.0, 32400.0, 10800.0, '이도현', '김지훈', '윤서아', '2024-12-01 17:00:00', '2025-01-02 14:25:00'),
('도심 속 케이던스 챌린지', 'CADENCE', 'REGISTRATION_OPEN', 'SCHEDULED', 'TEN_KM_COURSE', '2025-09-28', '2025-09-28 18:00:00', '2025-09-28 20:00:00', 45, 54000.0, 43200.0, 8100.0, 2700.0, NULL, NULL, NULL, '2025-08-15 10:30:00', '2025-08-15 10:30:00'),
('봄바람 휘날리며, 하프 마라톤', 'FLIGHT_TIME', 'PARTICIPATE_FINISH', 'FINISHED', 'HALF_COURSE', '2025-04-13', '2025-04-13 09:00:00', '2025-04-13 12:00:00', 310, 372000.0, 297600.0, 55800.0, 18600.0, '박선우', '정하윤', '김예준', '2025-02-20 15:00:00', '2025-04-14 16:10:00'),
('크리스마스 이브 5K 자선런', 'HEARTBEAT', 'REGISTRATION_OPEN', 'SCHEDULED', 'FIVE_KM_COURSE', '2025-12-24', '2025-12-24 19:00:00', '2025-12-24 20:00:00', 130, 156000.0, 124800.0, 23400.0, 7800.0, NULL, NULL, NULL, '2025-08-01 11:00:00', '2025-08-18 09:45:00');

INSERT INTO user (name, email, password, phone_number, birth, login_type, coupon, point, profile_url, watch_connected_key, create_date_time, modified_date_time) VALUES
('김민준', 'minjun.kim@example.com', 'encoded_password_1', '010-1234-5678', '1995-03-15', 'EMAIL', 5, 1500.0, 'http://example.com/profiles/1.jpg', null, '2025-08-21 12:30:01', '2025-08-21 12:30:01'),
('이서연', 'seoyeon.lee@example.com', null, '010-2345-6789', '1998-07-22', 'KAKAO', 2, 500.5, 'http://example.com/profiles/2.jpg', 'watch_key_001', '2025-08-21 12:30:02', '2025-08-21 12:30:02'),
('박도윤', 'doyun.park@example.com', 'encoded_password_3', '010-3456-7890', '1992-11-30', 'EMAIL', 10, 12500.0, null, null, '2025-08-21 12:30:03', '2025-08-21 12:30:03'),
('최지우', 'jiwoo.choi@example.com', null, '010-4567-8901', '2000-01-05', 'NAVER', 0, 0.0, 'http://example.com/profiles/4.jpg', null, '2025-08-21 12:30:04', '2025-08-21 12:30:04'),
('정하은', 'haeun.jeong@example.com', null, '010-5678-9012', '1999-05-19', 'GOOGLE', 3, 2200.0, 'http://example.com/profiles/5.jpg', 'watch_key_002', '2025-08-21 12:30:05', '2025-08-21 12:30:05'),
('강시우', 'siwoo.kang@example.com', 'encoded_password_6', '010-6789-0123', '1996-09-01', 'EMAIL', 1, 100.0, null, null, '2025-08-21 12:30:06', '2025-08-21 12:30:06'),
('조서아', 'seoa.jo@example.com', null, '010-7890-1234', '2001-02-28', 'APPLE', 7, 7800.0, 'http://example.com/profiles/7.jpg', 'watch_key_003', '2025-08-21 12:30:07', '2025-08-21 12:30:07'),
('윤예준', 'yejun.yoon@example.com', 'encoded_password_8', '010-8901-2345', '1993-08-10', 'EMAIL', 0, 50.0, null, null, '2025-08-21 12:30:08', '2025-08-21 12:30:08'),
('임하윤', 'hayun.lim@example.com', null, '010-9012-3456', '1997-12-25', 'KAKAO', 15, 30000.0, 'http://example.com/profiles/9.jpg', null, '2025-08-21 12:30:09', '2025-08-21 12:30:09'),
('한지호', 'jiho.han@example.com', 'encoded_password_10', '010-0123-4567', '2002-06-07', 'EMAIL', 4, 800.0, 'http://example.com/profiles/10.jpg', 'watch_key_004', '2025-08-21 12:30:10', '2025-08-21 12:30:10');

-- =================================================================
--  Payment Dummy Data (20 entries)
-- =================================================================

INSERT INTO payment (price, coupon_amount, state, payment_method, user_id, create_date_time, modified_date_time) VALUES
(10000, 1, 'PAYMENT_COMPLETED', 'CREDIT_CARD', 1, '2025-08-11 12:31:01', '2025-08-21 12:31:01'),
(5000, null, 'REFUND_COMPLETED', 'KAKAO_PAY', 2, '2025-08-11 12:31:02', '2025-08-21 12:35:00'),
(25000, 5, 'REFUND_REQUESTED', 'CREDIT_CARD', 3, '2025-08-01 12:31:03', '2025-08-21 12:31:03'),
(50000, 10, 'PAYMENT_COMPLETED', 'NAVER_PAY', 9, '2025-08-04 12:31:04', '2025-08-21 12:31:04'),
(10000, null, 'PAYMENT_COMPLETED', 'APPLE_PAY', 7, '2025-08-05 12:31:05', '2025-08-21 12:31:05'),
(5000, 1, 'REFUND_REJECTED', 'TOSS_PAY', 1, '2025-08-08 12:31:06', '2025-08-21 12:36:00'),
(15000, 2, 'PAYMENT_COMPLETED', 'CREDIT_CARD', 5, '2025-08-09 12:31:07', '2025-08-21 12:31:07'),
(30000, null, 'PAYMENT_COMPLETED', 'SAMSUNG_PAY', 10, '2025-08-11 12:31:08', '2025-08-21 12:31:08'),
(5000, 1, 'REFUND_COMPLETED', 'KAKAO_PAY', 4, '2025-08-19 12:31:09', '2025-08-21 12:37:00'),
(10000, 0, 'PAYMENT_COMPLETED', 'CREDIT_CARD', 8, '2025-08-01 12:31:10', '2025-08-21 12:31:10'),
(12000, 2, 'REFUND_REQUESTED', 'KAKAO_PAY', 1, '2025-08-02 12:51:01', '2025-08-21 12:51:01'),
(8000, null, 'REFUND_REQUESTED', 'NAVER_PAY', 2, '2025-08-03 12:51:02', '2025-08-21 12:51:02'),
(30000, 3, 'REFUND_REQUESTED', 'CREDIT_CARD', 3, '2025-08-04 12:51:03', '2025-08-21 12:51:03'),
(5000, 1, 'REFUND_REQUESTED', 'TOSS_PAY', 4, '2025-08-06 12:51:04', '2025-08-21 12:51:04'),
(18000, null, 'REFUND_REQUESTED', 'SAMSUNG_PAY', 5, '2025-08-15 12:51:05', '2025-08-21 12:51:05'),
(22000, 4, 'REFUND_REQUESTED', 'CREDIT_CARD', 6, '2025-08-19 12:51:06', '2025-08-21 12:51:06'),
(9000, 1, 'REFUND_REQUESTED', 'APPLE_PAY', 7, '2025-08-12 12:51:07', '2025-08-21 12:51:07'),
(45000, null, 'REFUND_REQUESTED', 'CREDIT_CARD', 8, '2025-08-10 12:51:08', '2025-08-21 12:51:08'),
(10000, 1, 'REFUND_REQUESTED', 'KAKAO_PAY', 9, '2025-08-09 12:51:09', '2025-08-21 12:51:09'),
(5000, 0, 'REFUND_REQUESTED', 'NAVER_PAY', 10, '2025-08-12 12:51:10', '2025-08-21 12:51:10');

-- =================================================================
--  Customer Inquiry Dummy Data (10 entries)
-- =================================================================

INSERT INTO customer_inquiry (payment_id, email, user_id, type, refund_price, refund_type, state, account_number, bank_name, title, message, answer, create_date_time, modified_date_time) VALUES
(3, 'doyun.park@example.com', 3, 'REFUND', 25000, 'MISTAKEN_PAYMENT', 'WAITING', '110-234-567890', '신한은행', '결제 환불 요청합니다.', '실수로 쿠폰 5개를 모두 사용해서 결제했습니다. 환불 부탁드립니다.', null, '2025-08-12 12:32:01', '2025-08-21 12:32:01'),
(null, 'jiwoo.choi@example.com', 4, 'ACCOUNT', null, null, 'SUCCESS', null, null, '계정 비밀번호를 잊어버렸습니다.', '네이버 아이디로 로그인하는데 비밀번호를 어떻게 찾을 수 있나요?', '네이버 로그인 페이지에서 비밀번호 찾기 기능을 이용해주시기 바랍니다.', '2025-08-11 12:32:02', '2025-08-21 12:38:00'),
(null, 'seoyeon.lee@example.com', 2, 'GAME', null, null, 'WAITING', null, null, '게임 기록이 이상합니다.', '어제 달리기 기록이 누락된 것 같습니다. 확인 부탁드립니다.', null, '2025-08-03 12:32:03', '2025-08-21 12:32:03'),
(7, 'haeun.jeong@example.com', 5, 'PAYMENT', null, null, 'SUCCESS', null, null, '결제 내역 확인 문의', '15000원 결제건에 대해 자세한 내역을 알고 싶습니다.', '해당 결제는 쿠폰 2개 구매에 대한 결제 내역으로 확인됩니다.', '2025-08-05 12:32:04', '2025-08-21 12:38:10'),
(null, 'hayun.lim@example.com', 9, 'APP', null, null, 'WAITING', null, null, '앱 사용법 질문', '포인트는 어떻게 현금으로 교환하나요?', null, '2025-08-07 12:32:05', '2025-08-21 12:32:05'),
(null, 'siwoo.kang@example.com', 6, 'USER', null, null, 'SUCCESS', null, null, '불량 유저 신고', '사용자 ID `aggressive_runner`가 비매너 행위를 합니다.', '신고 접수되었으며, 해당 유저에 대해 검토 후 조치하겠습니다.', '2025-08-11 12:32:06', '2025-08-21 12:38:20'),
(null, 'minjun.kim@example.com', 1, 'OTHER', null, null, 'SUCCESS', null, null, '파트너십 제휴 문의', '귀사의 서비스와 제휴를 맺고 싶습니다. 담당자분 연락처를 알 수 있을까요?', '제휴 문의는 business@hyper.run으로 메일 주시면 감사하겠습니다.', '2025-08-01 12:32:07', '2025-08-21 12:38:30'),
(9, 'jiwoo.choi@example.com', 4, 'REFUND', 5000, 'OTHER', 'SUCCESS', '1002-845-123456', '우리은행', '환불 완료 건 문의', '환불 요청 드렸던 5000원이 아직 입금되지 않았습니다.', '고객님, 확인 결과 금일 오후 중으로 입금 처리될 예정입니다. 불편을 드려 죄송합니다.', '2025-08-16 12:32:08', '2025-08-21 12:38:40'),
(null, 'jiho.han@example.com', 10, 'APP', null, null, 'WAITING', null, null, '워치 연결이 자꾸 끊깁니다.', '갤럭시 워치와 앱 연결이 불안정합니다. 해결 방법이 있나요?', null, '2025-08-01 12:32:09', '2025-08-21 12:32:09'),
(null, 'seoa.jo@example.com', 7, 'ACCOUNT', null, null, 'WAITING', null, null, '회원 탈퇴는 어떻게 하나요?', '회원 탈퇴 절차를 알려주세요.', null, '2025-08-14 12:32:10', '2025-08-21 12:32:10');

-- =================================================================
--  Exchange Transaction Dummy Data (10 entries)
-- =================================================================

INSERT INTO exchange_transaction (user_id, amount, account_number, bank_name, exchange_status, create_date_time, modified_date_time) VALUES
(9, 20000.0, '220-123-456789', '카카오뱅크', 'COMPLETED', '2025-08-12 12:40:01', '2025-08-21 12:45:00'),
(3, 10000.0, '110-234-567890', '신한은행', 'REQUESTED', '2025-08-13 12:40:02', '2025-08-21 12:40:02'),
(7, 5000.0, '3333-01-1234567', '토스뱅크', 'COMPLETED', '2025-08-11 12:40:03', '2025-08-21 12:46:00'),
(1, 1000.0, '1002-845-123456', '우리은행', 'CANCELLED', '2025-08-12 12:40:04', '2025-08-21 12:42:00'),
(5, 2000.0, '012-345-678901', 'KB국민은행', 'REQUESTED', '2025-08-15 12:40:05', '2025-08-21 12:40:05'),
(9, 5000.0, '220-123-456789', '카카오뱅크', 'COMPLETED', '2025-08-15 12:40:06', '2025-08-21 12:48:00'),
(2, 500.0, '9002-1234-5678-1', '새마을금고', 'REQUESTED', '2025-08-16 12:40:07', '2025-08-21 12:40:07'),
(10, 800.0, '456-78-901234', '하나은행', 'COMPLETED', '2025-08-20 12:40:08', '2025-08-21 12:50:00'),
(3, 2500.0, '110-234-567890', '신한은행', 'CANCELLED', '2025-08-16 12:40:09', '2025-08-21 12:41:00'),
(7, 2800.0, '3333-01-1234567', '토스뱅크', 'REQUESTED', '2025-08-08 12:40:10', '2025-08-21 12:40:10');
00', 8);