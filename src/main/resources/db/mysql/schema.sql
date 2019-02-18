DROP SCHEMA IF EXISTS `betting_crawler`;
CREATE SCHEMA `betting_crawler`;

GRANT ALL PRIVILEGES ON betting_crawler.* TO betting_crawler@localhost IDENTIFIED BY 'betting_crawler';

USE `betting_crawler`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `bettor`;
CREATE TABLE `bettor` (
    `bettor_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(150) DEFAULT NULL,
    PRIMARY KEY (`bettor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `sport`;
CREATE TABLE `sport` (
    `sport_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(150) DEFAULT NULL,
    `bettor_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`sport_id`),
    FOREIGN KEY (`bettor_id`) REFERENCES `bettor` (`bettor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
    `event_id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(150) DEFAULT NULL,
    `sport_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`event_id`),
    FOREIGN KEY (`sport_id`) REFERENCES `sport` (`sport_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
    `game_id` int(11) NOT NULL AUTO_INCREMENT,
    `game_code` int(11) DEFAULT NULL,
    `opponents` varchar(150) DEFAULT NULL,
    `date` date DEFAULT NULL,
    `time` time DEFAULT NULL,
    `event_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`game_id`),
    FOREIGN KEY (`event_id`) REFERENCES `event` (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `bet`;
CREATE TABLE `bet` (
    `bet_id` int(11) NOT NULL AUTO_INCREMENT,
    `home_win` decimal(5,2) DEFAULT NULL,
    `away_win` decimal(5,2) DEFAULT NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `game_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`bet_id`),
    FOREIGN KEY (`game_id`) REFERENCES `game` (`game_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

DROP TABLE IF EXISTS `football_bet`;
CREATE TABLE `football_bet` (
    `football_bet_id` int(11) NOT NULL AUTO_INCREMENT,
    `draw` decimal(5,2) DEFAULT NULL,
    FOREIGN KEY (`football_bet_id`) REFERENCES `bet` (`bet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1;

SET FOREIGN_KEY_CHECKS = 1;

ALTER DATABASE `betting_crawler` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bettor` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `sport` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `event` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `game` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bet` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `football_bet` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `football_bet`
ADD `over_0,5` decimal(5,2) DEFAULT NULL,
ADD `over_1,5` decimal(5,2) DEFAULT NULL,
ADD `over_2,5` decimal(5,2) DEFAULT NULL,
ADD `over_3,5` decimal(5,2) DEFAULT NULL,
ADD `over_4,5` decimal(5,2) DEFAULT NULL,
ADD `over_5,5` decimal(5,2) DEFAULT NULL,
ADD `over_6,5` decimal(5,2) DEFAULT NULL,
ADD `over_7,5` decimal(5,2) DEFAULT NULL,
ADD `under_0,5` decimal(5,2) DEFAULT NULL,
ADD `under_1,5` decimal(5,2) DEFAULT NULL,
ADD `under_2,5` decimal(5,2) DEFAULT NULL,
ADD `under_3,5` decimal(5,2) DEFAULT NULL,
ADD `under_4,5` decimal(5,2) DEFAULT NULL,
ADD `under_5,5` decimal(5,2) DEFAULT NULL,
ADD `under_6,5` decimal(5,2) DEFAULT NULL,
ADD `under_7,5` decimal(5,2) DEFAULT NULL,
ADD `GG` decimal(5,2) DEFAULT NULL,
ADD `NG` decimal(5,2) DEFAULT NULL,
ADD `1X` decimal(5,2) DEFAULT NULL,
ADD `2X` decimal(5,2) DEFAULT NULL,
ADD `12` decimal(5,2) DEFAULT NULL,
ADD `1/1` decimal(5,2) DEFAULT NULL,
ADD `X/1` decimal(5,2) DEFAULT NULL,
ADD `2/1` decimal(5,2) DEFAULT NULL,
ADD `1/X` decimal(5,2) DEFAULT NULL,
ADD `X/X` decimal(5,2) DEFAULT NULL,
ADD `2/X` decimal(5,2) DEFAULT NULL,
ADD `1/2` decimal(5,2) DEFAULT NULL,
ADD `X/2` decimal(5,2) DEFAULT NULL,
ADD `2/2` decimal(5,2) DEFAULT NULL;