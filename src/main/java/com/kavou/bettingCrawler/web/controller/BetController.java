package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.dao.BetRepository;
import com.kavou.bettingCrawler.web.dao.GameRepository;
import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BetController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private BetRepository betRepository;

    @GetMapping("/bets")
    private String getBetsByCategory(@RequestParam("gameId") int gameId, Model model, Pageable page) {

        // get the game of the bets
        Game game = gameRepository.getOneById(gameId);

        // get the bets of the game
        Page<Bet> bets = betRepository.findAllByGame(page, game);

        // find the bet category (football bet, tennis bet, ...)

        // get the sport of the game
        Sport sport = game.getEvent().getSport();

        // get the name of the sport
        String sportName = sport.getName();

        // add bets list to model
        model.addAttribute("bets", bets);
        // add game to model
        model.addAttribute("game", game);

        // return the appropriate page depending on sport name
        switch (sportName) {
            case "Ποδόσφαιρο":
                return "football-bets";
        }
        return null;
    }
}
