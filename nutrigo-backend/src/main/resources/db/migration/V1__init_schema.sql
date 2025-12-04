CREATE TABLE `analysis_item_option` (
                                        `analysis_item_option_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                        `external_option_name`	VARCHAR(100)	NOT NULL,
                                        `quantity`	INT	NOT NULL,
                                        `kcal_delta`	FLOAT	NULL,
                                        `sodium_mg_delta`	FLOAT	NULL,
                                        `analysis_item_id`	BIGINT	NOT NULL,
                                        `option_item_id`	BIGINT	NOT NULL,
                                        PRIMARY KEY (`analysis_item_option_id`)
);

CREATE TABLE `menu` (
                        `menu_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                        `name`	VARCHAR(100)	NOT NULL,
                        `base_price`	INT	NULL,
                        `is_active`	BOOLEAN	NOT NULL,
                        `created_at`	DATETIME	NOT NULL,
                        `updated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
                        `store_id`	BIGINT	NOT NULL,
                        `menu_category_id`	BIGINT	NOT NULL,
                        PRIMARY KEY (`menu_id`)
);

CREATE TABLE `menu_option_item` (
                                    `option_item_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                    `name`	VARCHAR(50)	NOT NULL,
                                    `price_delta`	INT	NOT NULL,
                                    `kcal_delta`	FLOAT	NULL,
                                    `sodium_mg_delta`	FLOAT	NULL,
                                    `is_default`	BOOLEAN	NOT NULL,
                                    `option_group_id`	BIGINT	NOT NULL,
                                    PRIMARY KEY (`option_item_id`)
);

CREATE TABLE `analysis_item` (
                                 `analysis_item_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                 `external_menu_name`	VARCHAR(100)	NOT NULL,
                                 `serving_size`	INT	NULL,
                                 `serving_unit`	VARCHAR(20)	NULL,
                                 `quantity`	INT	NULL,
                                 `estimated_kcal`	FLOAT	NULL,
                                 `estimated_sodium_mg`	FLOAT	NULL,
                                 `score`	FLOAT	NULL,
                                 `order_of_appearance`	INT	NULL,
                                 `analysis_id`	BIGINT	NOT NULL,
                                 `menu_id`	BIGINT	NULL,
                                 PRIMARY KEY (`analysis_item_id`)
);

CREATE TABLE `analysis_session` (
                                    `analysis_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                    `type`	ENUM('link','cart')	NOT NULL,
                                    `raw_input_type`	ENUM('url','image')	NOT NULL,
                                    `raw_input_value`	TEXT	NOT NULL,
                                    `total_kcal`	FLOAT	NULL,
                                    `total_sodium_mg`	FLOAT	NULL,
                                    `total_score`	FLOAT	NULL,
                                    `created_at`	DATETIME	NOT NULL,
                                    `user_id`	BIGINT	NOT NULL,
                                    `session_purpose`	ENUM('PRE_ORDER','RECORD')	NULL,
                                    `meal_time`	ENUM('BREAKFAST','LUNCH','DINNER','SNACK')	NULL,
                                    `meal_date`	DATE	NULL,
                                    PRIMARY KEY (`analysis_id`)
);

CREATE TABLE `alternative_suggestion` (
                                          `alternative_suggestion_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                          `reason`	VARCHAR(255)	NULL,
                                          `deltaKcal`	FLOAT	NULL,
                                          `deltaSodium`	FLOAT	NULL,
                                          `deltaScore`	FLOAT	NULL,
                                          `created_at`	DATETIME	NOT NULL,
                                          `suggest_menu_id`	BIGINT	NOT NULL,
                                          `analysis_item_id`	BIGINT	NOT NULL,
                                          PRIMARY KEY (`alternative_suggestion_id`)
);

CREATE TABLE `user_preferences` (
                                    `user_id`	BIGINT	NOT NULL,
                                    `portion_preference`	ENUM('SMALL','NORMAL','LARGE')	NULL,
                                    `health_mode`	ENUM('RELAXED','NORMAL','STRICT')	NULL,
                                    `updated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
                                    `evening_coach`	BOOLEAN	NULL,
                                    `challenge_reminder`	BOOLEAN	NULL,
                                    `default_mode`	ENUM('STRICT','BALANCED','FLEX')	NULL,
                                    PRIMARY KEY (`user_id`)
);

CREATE TABLE `challenge` (
                             `challenge_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                             `code`	VARCHAR(50)	NOT NULL,
                             `title`	VARCHAR(100)	NOT NULL,
                             `description`	TEXT	NULL,
                             `type`	ENUM('kcal','sodium','frequency','day_color','delivery_count','custom')	NOT NULL,
                             `duration_days`	INT	NOT NULL,
                             `created_at`	DATETIME	NOT NULL,
                             `updated_at`	DATETIME	NOT NULL,
                             PRIMARY KEY (`challenge_id`)
);

CREATE TABLE `nutrition_reference` (
                                       `nutrition_reference_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                       `category`	VARCHAR(100)	NOT NULL,
                                       `name`	VARCHAR(100)	NOT NULL,
                                       `kcal`	FLOAT	NULL,
                                       `protein_g`	FLOAT	NULL,
                                       `fat_g`	FLOAT	NULL,
                                       `carbs_g`	FLOAT	NULL,
                                       `sodium_mg`	FLOAT	NULL,
                                       `source`	VARCHAR(200)	NULL,
                                       PRIMARY KEY (`nutrition_reference_id`)
);

CREATE TABLE `daily_intake_summary` (
                                        `daily_intake_summary_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                        `user_id`	BIGINT	NOT NULL,
                                        `date`	DATE	NOT NULL,
                                        `total_kcal`	FLOAT	NULL,
                                        `total_sodium_mg`	FLOAT	NULL,
                                        `total_protein_g`	FLOAT	NULL,
                                        `created_at`	DATETIME	NOT NULL,
                                        `total_meals`	INT	NULL,
                                        `is_good_day`	BOOLEAN	NULL,
                                        `is_overeat_day`	BOOLEAN	NULL,
                                        `is_low_sodium_day`	BOOLEAN	NULL,
                                        `day_score`	FLOAT	NULL,
                                        PRIMARY KEY (`daily_intake_summary_id`)
);

CREATE TABLE `user` (
                        `user_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                        `email`	VARCHAR(100)	NOT NULL,
                        `nickname`	VARCHAR(50)	NULL,
                        `name`	VARCHAR(30)	NULL,
                        `gender`	ENUM('male','female','other')	NULL,
                        `birthday`	DATE	NULL,
                        `address`	VARCHAR(200)	NULL,
                        `created_at`	DATETIME	NOT NULL,
                        `updated_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`user_id`)
);

CREATE TABLE `menu_nutrition_estimate` (
                                           `nutrition_estimate_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                           `kcal`	FLOAT	NOT NULL,
                                           `sodium_mg`	FLOAT	NOT NULL,
                                           `protein_g`	FLOAT	NOT NULL,
                                           `confidence`	FLOAT	NULL,
                                           `created_at`	DATETIME	NOT NULL,
                                           `menu_id`	BIGINT	NOT NULL,
                                           `Field`	VARCHAR(255)	NULL,
                                           PRIMARY KEY (`nutrition_estimate_id`)
);

CREATE TABLE `user_challenge` (
                                  `user_challenge_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                  `started_at`	DATE	NOT NULL,
                                  `ended_at`	DATE	NULL,
                                  `finished_at`	DATE	NULL,
                                  `status`	ENUM('ongoing','completed','failed')	NOT NULL	DEFAULT 'ongoing',
                                  `progress_value`	FLOAT	NULL,
                                  `user_id`	BIGINT	NOT NULL,
                                  `challenge_id`	BIGINT	NOT NULL,
                                  PRIMARY KEY (`user_challenge_id`)
);

CREATE TABLE `menu_option_group` (
                                     `option_group_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                     `name`	VARCHAR(50)	NOT NULL,
                                     `select_type`	ENUM('single','multi','quantity','size','spiciness')	NOT NULL,
                                     `is_required`	BOOLEAN	NOT NULL,
                                     `menu_id`	BIGINT	NOT NULL,
                                     PRIMARY KEY (`option_group_id`)
);

CREATE TABLE `store` (
                         `store_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                         `name`	VARCHAR(100)	NOT NULL,
                         `provider`	ENUM('baemin','yogiyo','coupang','other')	NOT NULL,
                         `external_store_id`	VARCHAR(100)	NULL,
                         `region_code`	VARCHAR(50)	NULL,
                         `address`	VARCHAR(255)	NULL,
                         `created_at`	DATETIME	NOT NULL,
                         PRIMARY KEY (`store_id`)
);

CREATE TABLE `menu_category` (
                                 `menu_category_id`	BIGINT	NOT NULL AUTO_INCREMENT,
                                 `name`	VARCHAR(50)	NOT NULL,
                                 `parent_id`	BIGINT	NULL,
                                 PRIMARY KEY (`menu_category_id`)
);

ALTER TABLE `user_preferences`
    ADD CONSTRAINT `FK_user_TO_user_preferences_1`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`);
