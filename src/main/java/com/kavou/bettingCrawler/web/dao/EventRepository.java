package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {

    boolean existsByNameAndSport(String name, Sport sport);
    Event getOneByName(String name);
}
