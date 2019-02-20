package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Game;
import com.kavou.bettingCrawler.web.service.EventService;
import com.kavou.bettingCrawler.web.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private EventService eventService;

    @GetMapping("/games")
    public String getGames(@RequestParam("eventId") int eventId, Model theModel,  @SortDefault("opponents") Pageable page) {

        // get the event of the games
        Event event = eventService.getOneById(eventId);

        // get the list of games from db
        Page<Game> theGames = gameService.findAllByEvent(page, event);

        // add game list to model
        theModel.addAttribute("games", theGames);
        // add event to model
        theModel.addAttribute("event", event);

        return "games";
    }
}
