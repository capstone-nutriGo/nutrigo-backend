CREATE TABLE analysis_session
(
    analysis_id     BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime NOT NULL,
    meal_date       date NULL,
    meal_time       ENUM NULL,
    raw_input_type  ENUM     NOT NULL,
    raw_input_value LONGTEXT NOT NULL,
    session_purpose ENUM NULL,
    total_kcal      FLOAT NULL,
    total_score     FLOAT NULL,
    total_sodium_mg FLOAT NULL,
    type            ENUM     NOT NULL,
    user_id         BIGINT   NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (analysis_id)
);

CREATE TABLE challenge
(
    challenge_id  BIGINT AUTO_INCREMENT NOT NULL,
    category      ENUM NULL,
    code          VARCHAR(50)  NOT NULL,
    created_at    datetime     NOT NULL,
    `description` LONGTEXT NULL,
    duration_days INT          NOT NULL,
    status        ENUM NULL,
    title         VARCHAR(100) NOT NULL,
    type          ENUM         NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (challenge_id)
);

CREATE TABLE daily_intake_summary
(
    daily_intake_summary_id BIGINT AUTO_INCREMENT NOT NULL,
    created_at              datetime NOT NULL,
    date                    date     NOT NULL,
    day_score               FLOAT NULL,
    is_good_day             BIT(1) NULL,
    is_low_sodium_day       BIT(1) NULL,
    is_overeat_day          BIT(1) NULL,
    total_kcal              FLOAT NULL,
    total_meals             INT NULL,
    total_protein_g         FLOAT NULL,
    total_sodium_mg         FLOAT NULL,
    user_id                 BIGINT   NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (daily_intake_summary_id)
);

CREATE TABLE menu
(
    menu_id          BIGINT AUTO_INCREMENT NOT NULL,
    is_active        BIT(1)       NOT NULL,
    base_price       INT NULL,
    created_at       datetime     NOT NULL,
    menu_category_id BIGINT       NOT NULL,
    name             VARCHAR(100) NOT NULL,
    store_id         BIGINT       NOT NULL,
    updated_at       datetime     NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (menu_id)
);

CREATE TABLE store
(
    store_id          BIGINT AUTO_INCREMENT NOT NULL,
    address           VARCHAR(255) NULL,
    created_at        datetime     NOT NULL,
    external_store_id VARCHAR(100) NULL,
    name              VARCHAR(100) NOT NULL,
    provider          ENUM         NOT NULL,
    region_code       VARCHAR(50) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (store_id)
);

CREATE TABLE user
(
    user_id    BIGINT AUTO_INCREMENT NOT NULL,
    address    VARCHAR(200) NULL,
    birthday   date NULL,
    created_at datetime     NOT NULL,
    email      VARCHAR(100) NOT NULL,
    gender     ENUM NULL,
    name       VARCHAR(30) NULL,
    nickname   VARCHAR(50) NULL,
    updated_at datetime     NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (user_id)
);

CREATE TABLE user_preferences
(
    user_id            BIGINT   NOT NULL,
    challenge_reminder BIT(1) NULL,
    default_mode       ENUM NULL,
    evening_coach      BIT(1) NULL,
    health_mode        ENUM NULL,
    portion_preference ENUM NULL,
    updated_at         datetime NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (user_id)
);

ALTER TABLE user_preferences
    ADD CONSTRAINT FK6kbacckv752q9rtpnq7h6jeyo FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE NO ACTION;

ALTER TABLE daily_intake_summary
    ADD CONSTRAINT FK9biebbstn0aosw1vwpew6g5hk FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE NO ACTION;

CREATE INDEX FK9biebbstn0aosw1vwpew6g5hk ON daily_intake_summary (user_id);

ALTER TABLE analysis_session
    ADD CONSTRAINT FKie0juppooipaq6s77ifylt8ce FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE NO ACTION;

CREATE INDEX FKie0juppooipaq6s77ifylt8ce ON analysis_session (user_id);