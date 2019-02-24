package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TennisBetService {

    Page<Bet> findAllByGame(Pageable page, Game game);
}
