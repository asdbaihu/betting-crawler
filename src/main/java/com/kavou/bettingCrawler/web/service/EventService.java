package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    Event getOneById(int eventId);
    Page<Event> findAllBySport(Pageable page, Sport sport);
}
