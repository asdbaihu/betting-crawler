package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.FootballBet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FootballBetRepository extends JpaRepository<FootballBet, Integer> {
}
