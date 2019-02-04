--OneToMany (one game -> many bets)

DROP SCHEMA IF EXISTS `betting_crawler`;

CREATE SCHEMA `betting_crawler`;

use `betting_crawler`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `game`;

CREATE TABLE `game` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `sport` varchar(45) DEFAULT NULL,
    `bettor` varchar(45) DEFAULT NULL,
    `match_code` int(11) DEFAULT NULL,
    `event` varchar(85) DEFAULT NULL,
    `opponents` varchar(85) DEFAULT NULL,
    `date` date DEFAULT NULL,
    `time` time DEFAULT NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `bet`;

CREATE TABLE `bet` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `sport` varchar(45) DEFAULT NULL,
    `bettor` varchar(45) DEFAULT NULL,
    `home_win` decimal(5,2) DEFAULT NULL,
    `away_win` decimal(5,2) DEFAULT NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `game_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`game_id`) REFERENCES `game` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `football_bet`;

CREATE TABLE `football_bet` (
    `bet_id` int(11) NOT NULL AUTO_INCREMENT,
    `draw` decimal(5,2) DEFAULT NULL,
    FOREIGN KEY (`bet_id`) REFERENCES `bet` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

SET FOREIGN_KEY_CHECKS = 1;

--Database accept greek characters

ALTER DATABASE `betting_crawler` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `game` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `bet` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `football_bet` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

--Added extra fields to football_bet table

ALTER TABLE football_bet
ADD `over_0,5` decimal(5,2) DEFAULT NULL,
ADD `over_1,5` decimal(5,2) DEFAULT NULL,
ADD `over_2,5` decimal(5,2) DEFAULT NULL,
ADD `over_3,5` decimal(5,2) DEFAULT NULL,
ADD `over_4,5` decimal(5,2) DEFAULT NULL,
ADD `over_5,5` decimal(5,2) DEFAULT NULL,
ADD `over_6,5` decimal(5,2) DEFAULT NULL,
ADD `under_0,5` decimal(5,2) DEFAULT NULL,
ADD `under_1,5` decimal(5,2) DEFAULT NULL,
ADD `under_2,5` decimal(5,2) DEFAULT NULL,
ADD `under_3,5` decimal(5,2) DEFAULT NULL,
ADD `under_4,5` decimal(5,2) DEFAULT NULL,
ADD `under_5,5` decimal(5,2) DEFAULT NULL,
ADD `under_6,5` decimal(5,2) DEFAULT NULL,
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
ADD `2/2` decimal(5,2) DEFAULT NULL
;