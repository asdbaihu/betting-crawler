package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.BasketballBetRepository;
import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BasketballBetServiceImpl implements BetService {

    @Autowired
    BasketballBetRepository basketballBetRepository;

    @Override
    public Page<Bet> findAllByGame(Pageable page, Game game) {
        return basketballBetRepository.findAllByGame(page, game);
    }
}
