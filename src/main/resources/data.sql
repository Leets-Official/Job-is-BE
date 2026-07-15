-- 1. 지역 메타데이터 주입
INSERT INTO regions (id, name) VALUES
                                   (1, '서울'),
                                   (2, '경기'),
                                   (3, '인천'),
                                   (4, '부산'),
                                   (5, '대구'),
                                   (6, '광주'),
                                   (7, '대전'),
                                   (8, '울산'),
                                   (9, '세종'),
                                   (10, '강원'),
                                   (11, '충북'),
                                   (12, '충남'),
                                   (13, '전북'),
                                   (14, '전남'),
                                   (15, '경북'),
                                   (16, '경남'),
                                   (17, '제주')
ON CONFLICT (id) DO NOTHING;

-- PostgreSQL 자동 증가 시퀀스 번호 동기화
SELECT setval(pg_get_serial_sequence('regions', 'id'), coalesce(max(id), 1)) FROM regions;


-- 직무 카테고리 주입
INSERT INTO job_categories (id, name) VALUES
                                          (1, '백엔드 개발자'),
                                          (2, '프론트엔드 개발자'),
                                          (3, '풀스택 개발자'),
                                          (4, 'iOS 개발자'),
                                          (5, 'Android 개발자'),
                                          (6, '데이터 엔지니어'),
                                          (7, '데이터 분석가'),
                                          (8, '머신러닝 엔지니어'),
                                          (9, 'DevOps 엔지니어'),
                                          (10, '인프라 엔지니어'),
                                          (11, 'QA 엔지니어'),
                                          (12, '보안 엔지니어'),
                                          (13, '임베디드 SW 개발자')
ON CONFLICT (id) DO NOTHING;

-- PostgreSQL 자동 증가 시퀀스 번호 동기화 (job_categories)
SELECT setval(pg_get_serial_sequence('job_categories', 'id'), coalesce(max(id), 1)) FROM job_categories;
