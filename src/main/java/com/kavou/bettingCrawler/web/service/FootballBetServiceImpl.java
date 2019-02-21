package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.FootballBetRepository;
import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FootballBetServiceImpl implements FootballBetService {

    @Autowired
    FootballBetRepository footballBetRepository;

    @Override
    public Page<Bet> findAllByGame(Pageable page, Game game) {
        return footballBetRepository.findAllByGame(page, game);
    }
}
