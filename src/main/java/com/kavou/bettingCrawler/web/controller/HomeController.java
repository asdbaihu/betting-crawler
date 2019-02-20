package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.service.BettorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private BettorService bettorService;

    @GetMapping("/")
    public String getBettors(Model theModel,  @SortDefault("name") Pageable page) {

        // get the list of bettors from db
        Page<Bettor> theBettors = bettorService.findAll(page);

        // add bettor list to model
        theModel.addAttribute("bettors", theBettors);

        return "index";
    }

}

