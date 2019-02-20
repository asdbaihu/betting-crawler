package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameService {

    Page<Game> findAllByEvent(Pageable page, Event event);
    Game getOneById(int gameId);
}
