package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.GameRepository;
import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private GameRepository gameRepository;

    @Override
    public Page<Game> findAllByEvent(Pageable page, Event event) {
        return gameRepository.findAllByEvent(page, event);
    }

    @Override
    public Game getOneById(int gameId) {
        return gameRepository.getOneById(gameId);
    }
}
