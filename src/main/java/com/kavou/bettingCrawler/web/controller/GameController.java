package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.entity.Bet;
import com.kavou.bettingCrawler.web.entity.Game;
import com.kavou.bettingCrawler.web.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/list")
    public String listGames(Model theModel, @SortDefault("opponents") Pageable page) {

        // get the list of game from db
        Page<Game> theGames = gameService.findAll(page);

        // add game list to model
        theModel.addAttribute("games", theGames);

        return "list-games";
    }
}
