package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import com.kavou.bettingCrawler.web.entity.TennisBet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TennisBetRepository extends JpaRepository<TennisBet, String> {

    Page<Bet> findAllByGame(Pageable page, Game game);
}
