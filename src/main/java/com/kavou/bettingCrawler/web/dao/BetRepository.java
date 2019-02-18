package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Integer> {

}
