CREATE TABLE `meal_log` (
                            `meal_log_id` BIGINT NOT NULL AUTO_INCREMENT,
                            `source` VARCHAR(50) NOT NULL,
                            `analysis_id` BIGINT NOT NULL,
                            `meal_time` ENUM('BREAKFAST','LUNCH','DINNER','SNACK') NULL,
                            `ordered_at` DATETIME(6) NOT NULL,
                            `created_at` DATETIME(6) NOT NULL,

                            PRIMARY KEY (`meal_log_id`),
                            CONSTRAINT `fk_meal_log_analysis_session`
                                FOREIGN KEY (`analysis_id`) REFERENCES `analysis_session`(`analysis_id`)
);

ALTER TABLE `user`
    ADD COLUMN `password` VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE `challenge`
    ADD COLUMN `status`
        ENUM('ACTIVE','INACTIVE') NOT NULL
        AFTER `type`,
    ADD COLUMN `category`
        ENUM('HEALTH','FUN') NULL
        AFTER `status`,
    ADD COLUMN `is_custom`
        TINYINT(1) NULL
        AFTER `category`,
    ADD COLUMN `created_by`
        BIGINT NULL
        AFTER `is_custom`,
    ADD COLUMN `target_count`
        INT NULL
        AFTER `created_by`,
    ADD COLUMN `max_kcal_per_meal`
        INT NULL
        AFTER `target_count`,
    ADD COLUMN `max_sodium_mg_per_meal`
        INT NULL
        AFTER `max_kcal_per_meal`,
    ADD COLUMN `custom_description`
        TEXT NULL
        AFTER `max_sodium_mg_per_meal`,
    ADD CONSTRAINT `fk_challenge_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `user`(`user_id`)
        ON DELETE SET NULL
           ON UPDATE CASCADE;

ALTER TABLE `user_challenge`
    MODIFY COLUMN `started_at`	DATETIME	NOT NULL,
    MODIFY COLUMN `ended_at`	DATETIME	NULL,
    MODIFY COLUMN `finished_at`	DATETIME	NULL,
    ADD COLUMN `logs_count`
        INT NOT NULL
        AFTER `progress_rate`;
ALTER TABLE challenge
    MODIFY COLUMN updated_at datetime(6)
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6);
