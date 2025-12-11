-- Ensure backup before applying schema changes
-- mysqldump --single-transaction -u<user> -p<password> <database> > backup.sql

-- Align defaults and unique constraints on users
ALTER TABLE `user`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT `uk_user_email` UNIQUE (`email`);

ALTER TABLE `user_preferences`
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `store`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE `menu_category`
    ADD INDEX `idx_menu_category_parent_id` (`parent_id`),
    ADD CONSTRAINT `fk_menu_category_parent` FOREIGN KEY (`parent_id`)
        REFERENCES `menu_category`(`menu_category_id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

ALTER TABLE `menu`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD INDEX `idx_menu_store_id` (`store_id`),
    ADD INDEX `idx_menu_category_id` (`menu_category_id`),
    ADD CONSTRAINT `fk_menu_store` FOREIGN KEY (`store_id`) REFERENCES `store`(`store_id`),
    ADD CONSTRAINT `fk_menu_category` FOREIGN KEY (`menu_category_id`) REFERENCES `menu_category`(`menu_category_id`);

ALTER TABLE `menu_option_group`
    ADD INDEX `idx_menu_option_group_menu_id` (`menu_id`),
    ADD CONSTRAINT `fk_menu_option_group_menu` FOREIGN KEY (`menu_id`)
        REFERENCES `menu`(`menu_id`)
        ON DELETE CASCADE;

ALTER TABLE `menu_option_item`
    ADD INDEX `idx_menu_option_item_group_id` (`option_group_id`),
    ADD CONSTRAINT `fk_menu_option_item_group` FOREIGN KEY (`option_group_id`)
        REFERENCES `menu_option_group`(`option_group_id`)
        ON DELETE CASCADE;

ALTER TABLE `menu_nutrition_estimate`
    ADD INDEX `idx_menu_nutrition_estimate_menu_id` (`menu_id`),
    ADD CONSTRAINT `fk_menu_nutrition_estimate_menu` FOREIGN KEY (`menu_id`)
        REFERENCES `menu`(`menu_id`);

ALTER TABLE `analysis_session`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD INDEX `idx_analysis_session_user_id` (`user_id`),
    ADD CONSTRAINT `fk_analysis_session_user` FOREIGN KEY (`user_id`)
        REFERENCES `user`(`user_id`);

ALTER TABLE `analysis_item`
    ADD INDEX `idx_analysis_item_analysis_id` (`analysis_id`),
    ADD INDEX `idx_analysis_item_menu_id` (`menu_id`),
    ADD CONSTRAINT `fk_analysis_item_session` FOREIGN KEY (`analysis_id`)
        REFERENCES `analysis_session`(`analysis_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_analysis_item_menu` FOREIGN KEY (`menu_id`)
        REFERENCES `menu`(`menu_id`);

ALTER TABLE `analysis_item_option`
    ADD INDEX `idx_analysis_item_option_item` (`analysis_item_id`),
    ADD INDEX `idx_analysis_item_option_option_item` (`option_item_id`),
    ADD CONSTRAINT `fk_analysis_item_option_analysis_item` FOREIGN KEY (`analysis_item_id`)
        REFERENCES `analysis_item`(`analysis_item_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_analysis_item_option_option_item` FOREIGN KEY (`option_item_id`)
        REFERENCES `menu_option_item`(`option_item_id`);

ALTER TABLE `alternative_suggestion`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD INDEX `idx_alternative_suggestion_menu_id` (`suggest_menu_id`),
    ADD INDEX `idx_alternative_suggestion_analysis_item_id` (`analysis_item_id`),
    ADD CONSTRAINT `fk_alternative_suggestion_menu` FOREIGN KEY (`suggest_menu_id`)
        REFERENCES `menu`(`menu_id`),
    ADD CONSTRAINT `fk_alternative_suggestion_analysis_item` FOREIGN KEY (`analysis_item_id`)
        REFERENCES `analysis_item`(`analysis_item_id`)
        ON DELETE CASCADE;

ALTER TABLE `daily_intake_summary`
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD INDEX `idx_daily_intake_summary_user_id` (`user_id`),
    ADD CONSTRAINT `fk_daily_intake_summary_user` FOREIGN KEY (`user_id`)
        REFERENCES `user`(`user_id`);

ALTER TABLE `challenge`
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

ALTER TABLE `user_challenge`
    ADD INDEX `idx_user_challenge_user_id` (`user_id`),
    ADD INDEX `idx_user_challenge_challenge_id` (`challenge_id`),
    ADD CONSTRAINT `fk_user_challenge_user` FOREIGN KEY (`user_id`)
        REFERENCES `user`(`user_id`),
    ADD CONSTRAINT `fk_user_challenge_challenge` FOREIGN KEY (`challenge_id`)
        REFERENCES `challenge`(`challenge_id`);

ALTER TABLE `meal_log`
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- Align meal_log schema with new meal tracking requirements
ALTER TABLE `meal_log`
    DROP FOREIGN KEY `fk_meal_log_analysis_session`,
    DROP COLUMN `source`,
    DROP COLUMN `analysis_id`,
    DROP COLUMN `ordered_at`,
    ADD COLUMN `menu` VARCHAR(50) NULL AFTER `meal_log_id`,
    ADD COLUMN `kcal` FLOAT NULL AFTER `menu`,
    ADD COLUMN `sodium_mg` FLOAT NULL AFTER `kcal`,
    ADD COLUMN `protein_g` FLOAT NULL AFTER `sodium_mg`,
    ADD COLUMN `carb_g` FLOAT NULL AFTER `protein_g`,
    ADD COLUMN `total_score` FLOAT NULL AFTER `carb_g`,
    MODIFY COLUMN `meal_time` ENUM('BREAKFAST','LUNCH','DINNER','SNACK','NIGHT') NULL,
    ADD COLUMN `meal_date` DATE NULL AFTER `meal_time`,
    ADD COLUMN `daily_intake_summary_id` BIGINT NOT NULL AFTER `meal_date`,
    MODIFY COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT `fk_meal_log_daily_intake_summary`
        FOREIGN KEY (`daily_intake_summary_id`) REFERENCES `daily_intake_summary`(`daily_intake_summary_id`)
            ON DELETE CASCADE;

-- Align schema with requested table definitions

-- Update user table to enforce required attributes and defaults
ALTER TABLE `user`
    DROP COLUMN `address`,
    MODIFY COLUMN `email` VARCHAR(100) NOT NULL,
    MODIFY COLUMN `password` VARCHAR(255) NOT NULL,
    MODIFY COLUMN `nickname` VARCHAR(50) NOT NULL,
    MODIFY COLUMN `name` VARCHAR(30) NOT NULL,
    MODIFY COLUMN `gender` ENUM('male','female','other') NOT NULL,
    MODIFY COLUMN `birthday` DATE NOT NULL,
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Bring user_preferences in line with user_setting shape
ALTER TABLE `user_preferences`
    DROP FOREIGN KEY `FK_user_TO_user_preferences_1`;

RENAME TABLE `user_preferences` TO `user_setting`;

ALTER TABLE `user_setting`
    DROP COLUMN `portion_preference`,
    DROP COLUMN `health_mode`,
    DROP COLUMN `default_mode`,
    MODIFY COLUMN `evening_coach` BOOLEAN NOT NULL DEFAULT TRUE,
    MODIFY COLUMN `challenge_reminder` BOOLEAN NOT NULL DEFAULT TRUE,
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE `user_setting`
    ADD CONSTRAINT `FK_user_TO_user_setting_1`
        FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`);

-- Align challenge definition
ALTER TABLE `challenge`
    MODIFY COLUMN `type` ENUM('kcal','sodium','protein','frequency','day_color') NOT NULL,
    MODIFY COLUMN `duration_days` INT NOT NULL,
    MODIFY COLUMN `status` ENUM('ACTIVE','INACTIVE') NULL,
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN `updated_at` DATETIME(6) NULL;



-- Adjust user_challenge to requested shape
ALTER TABLE `user_challenge`
    MODIFY COLUMN `started_at` DATE NOT NULL,
    MODIFY COLUMN `ended_at` DATE NULL,
    MODIFY COLUMN `finished_at` DATETIME NULL,
    MODIFY COLUMN `status` ENUM('ongoing','completed','failed') NOT NULL DEFAULT 'ongoing',
    DROP COLUMN `logs_count`;

-- Daily intake summary changes
ALTER TABLE `daily_intake_summary`
    ADD COLUMN `total_carb_g` FLOAT NULL AFTER `total_protein_g`,
    ADD COLUMN `total_snack` INT NULL AFTER `total_carb_g`,
    ADD COLUMN `total_night` INT NULL AFTER `total_snack`,
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) AFTER `created_at`,
    MODIFY COLUMN `day_score` FLOAT NULL,
    ADD COLUMN `day_color` ENUM('red','yellow','green') NOT NULL DEFAULT 'green' AFTER `day_score`,
    DROP COLUMN `total_meals`,
    DROP COLUMN `is_good_day`,
    DROP COLUMN `is_overeat_day`,
    DROP COLUMN `is_low_sodium_day`;

-- Meal log alignment: column rename and defaults
ALTER TABLE `meal_log`
    DROP FOREIGN KEY `fk_meal_log_daily_intake_summary`;

ALTER TABLE `meal_log`
    MODIFY COLUMN `daily_intake_summary_id` BIGINT NOT NULL,
    MODIFY COLUMN `created_at` DATETIME NOT NULL;

ALTER TABLE `meal_log`
    ADD CONSTRAINT `fk_meal_log_daily_intake_summary`
        FOREIGN KEY (`daily_intake_summary_id`)
            REFERENCES `daily_intake_summary`(`daily_intake_summary_id`)
            ON DELETE CASCADE;
