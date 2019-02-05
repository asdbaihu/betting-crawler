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
);

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
);

DROP TABLE IF EXISTS `football_bet`;

CREATE TABLE `football_bet` (
    `bet_id` int(11) NOT NULL AUTO_INCREMENT,
    `draw` decimal(5,2) DEFAULT NULL,
    FOREIGN KEY (`bet_id`) REFERENCES `bet` (`id`)
);

ALTER TABLE `football_bet`
ADD (
`over_0,5` decimal(5,2) DEFAULT NULL,
`over_1,5` decimal(5,2) DEFAULT NULL,
`over_2,5` decimal(5,2) DEFAULT NULL,
`over_3,5` decimal(5,2) DEFAULT NULL,
`over_4,5` decimal(5,2) DEFAULT NULL,
`over_5,5` decimal(5,2) DEFAULT NULL,
`over_6,5` decimal(5,2) DEFAULT NULL,
`under_0,5` decimal(5,2) DEFAULT NULL,
`under_1,5` decimal(5,2) DEFAULT NULL,
`under_2,5` decimal(5,2) DEFAULT NULL,
`under_3,5` decimal(5,2) DEFAULT NULL,
`under_4,5` decimal(5,2) DEFAULT NULL,
`under_5,5` decimal(5,2) DEFAULT NULL,
`under_6,5` decimal(5,2) DEFAULT NULL,
`GG` decimal(5,2) DEFAULT NULL,
`NG` decimal(5,2) DEFAULT NULL,
`1X` decimal(5,2) DEFAULT NULL,
`2X` decimal(5,2) DEFAULT NULL,
`12` decimal(5,2) DEFAULT NULL,
`1/1` decimal(5,2) DEFAULT NULL,
`X/1` decimal(5,2) DEFAULT NULL,
`2/1` decimal(5,2) DEFAULT NULL,
`1/X` decimal(5,2) DEFAULT NULL,
`X/X` decimal(5,2) DEFAULT NULL,
`2/X` decimal(5,2) DEFAULT NULL,
`1/2` decimal(5,2) DEFAULT NULL,
`X/2` decimal(5,2) DEFAULT NULL,
`2/2` decimal(5,2) DEFAULT NULL
) after `draw`;
