CREATE TABLE `analysis_session` (
                                    `analysis_id`      BIGINT NOT NULL AUTO_INCREMENT,
                                    `type`             ENUM('link','cart') NOT NULL,
                                    `raw_input_type`   ENUM('url','image') NOT NULL,
                                    `raw_input_value`  TEXT NOT NULL,
                                    `total_kcal`       FLOAT NULL,
                                    `total_sodium_mg`  FLOAT NULL,
                                    `total_score`      FLOAT NULL,
                                    `created_at`       DATETIME NOT NULL,
                                    `user_id`          BIGINT NOT NULL,
                                    `session_purpose`  ENUM('PRE_ORDER','RECORD') NULL,
                                    `meal_time`        ENUM('BREAKFAST','LUNCH','DINNER','SNACK') NULL,
                                    `meal_date`        DATE NULL,
                                    PRIMARY KEY (`analysis_id`)
    -- 외래키까지 걸고 싶으면 User 테이블 구조 보고 아래 주석 풀어서 수정하면 돼
    -- ,CONSTRAINT `fk_analysis_session_user`
    --   FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;