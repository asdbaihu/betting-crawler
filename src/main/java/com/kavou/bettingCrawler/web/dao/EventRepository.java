package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {

    boolean existsByNameAndSport(String name, Sport sport);

    Event getOneByNameAndSport(String name, Sport sport);
    Event getOneById(int eventId);

    Page<Event> findAllBySport(Pageable page, Sport sport);
}
