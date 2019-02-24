package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.dao.BasketballBetRepository;
import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import com.kavou.bettingCrawler.web.entity.Sport;
import com.kavou.bettingCrawler.web.service.BasketballBetService;
import com.kavou.bettingCrawler.web.service.FootballBetService;
import com.kavou.bettingCrawler.web.service.GameService;
import com.kavou.bettingCrawler.web.service.TennisBetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BetController {

    @Autowired
    private GameService gameService;

    @Autowired
    private FootballBetService footballBetService;

    @Autowired
    private BasketballBetService basketballBetService;

    @Autowired
    private TennisBetService tennisBetService;

    @GetMapping("/bets")
    private String getBetsByCategory(@RequestParam("gameId") int gameId, Model model, Pageable page) {

        // get the game of the bets
        Game game = gameService.getOneById(gameId);

        // find the bet category (football bet, tennis bet, ...)
        // get the sport of the game
        Sport sport = game.getEvent().getSport();

        // get the name of the sport
        String sportName = sport.getName();

        // return the appropriate page depending on sport name
        switch (sportName) {

            case "Ποδόσφαιρο":

                // get the bets of the game
                Page<Bet> footballBets = footballBetService.findAllByGame(page, game);

                // create a list with the bets
                List<Bet> footballBetsList = footballBets.getContent();

                // add bets list to model
                model.addAttribute("bets", footballBets);
                // add bets list to model
                model.addAttribute("betsList", footballBetsList);
                // add game to model
                model.addAttribute("game", game);

                return "football-bets";

            case "Μπάσκετ":

                // get the bets of the game
                Page<Bet> basketballBets = basketballBetService.findAllByGame(page, game);

                // create a list with the bets
                List<Bet> basketballBetsList = basketballBets.getContent();

                // add bets list to model
                model.addAttribute("bets", basketballBets);
                // add bets list to model
                model.addAttribute("betsList", basketballBetsList);
                // add game to model
                model.addAttribute("game", game);

                return "basketball-bets";

            case "Τένις":

                // get the bets of the game
                Page<Bet> tennisBets = tennisBetService.findAllByGame(page, game);

                // create a list with the bets
                List<Bet> tennisBetsList = tennisBets.getContent();

                // add bets list to model
                model.addAttribute("bets", tennisBets);
                // add bets list to model
                model.addAttribute("betsList", tennisBetsList);
                // add game to model
                model.addAttribute("game", game);

                return "tennis-bets";
        }

        return null;
    }
}
