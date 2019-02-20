package com.kavou.bettingCrawler.web.controller;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import com.kavou.bettingCrawler.web.service.EventService;
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
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    SportService sportService;

    @GetMapping("/events")
    public String getEvents(@RequestParam("sportId") int sportId, Model theModel,  @SortDefault("name") Pageable page) {

        // get the sport of the event
        Sport sport = sportService.getOneById(sportId);

        // get the list of sports from db
        Page<Event> events = eventService.findAllBySport(page, sport);

        // add game list to model
        theModel.addAttribute("events", events);
        // add sport to model
        theModel.addAttribute("sport", sport);

        return "events";

    }
}
