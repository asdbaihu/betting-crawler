package com.kavou.bettingCrawler.web.api.repositories.betRepositories;

import com.kavou.bettingCrawler.web.api.entities.betEntities.FootballBet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FootballBetRepository extends JpaRepository<FootballBet, Integer> {
}
