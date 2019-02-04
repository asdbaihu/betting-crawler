package com.kavou.bettingCrawler.web.api.repositories;

import com.kavou.bettingCrawler.web.api.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {

}
