package com.kavou.bettingCrawler.web.api.repositories;

import com.kavou.bettingCrawler.web.api.entities.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Integer> {
}
