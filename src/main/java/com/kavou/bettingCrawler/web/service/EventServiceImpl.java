package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.EventRepository;
import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    EventRepository eventRepository;

    @Override
    public Event getOneById(int eventId) {
        return eventRepository.getOneById(eventId);
    }

    @Override
    public Page<Event> findAllBySport(Pageable page, Sport sport) {
        return
                eventRepository.findAllBySport(page, sport);
    }
}
