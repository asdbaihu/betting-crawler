package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Sport;
import com.kavou.bettingCrawler.web.service.BettorService;
import com.kavou.bettingCrawler.web.service.SportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SportController {

    @Autowired
    private SportService sportService;

    @Autowired
    private BettorService bettorService;

    @GetMapping("/sports")
    public String getSports(@RequestParam("bettorId") int bettorId, Model theModel, Pageable page) {

        // get the bettor of the sports
        Bettor bettor = bettorService.getOneById(bettorId);

        // get the list of sports from db
        Page<Sport> sports = sportService.findAllByBettor(page, bettor);

        // add game list to model
        theModel.addAttribute("sports", sports);
        // add bettor to model
        theModel.addAttribute("bettor", bettor);

        return "sports";
    }

}
