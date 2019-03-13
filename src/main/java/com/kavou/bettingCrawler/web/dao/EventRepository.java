package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Event;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    boolean existsByNameAndSport(String name, Sport sport);

    Event getOneByNameAndSport(String name, Sport sport);
    Event getOneById(int eventId);

    Page<Event> findAllBySport(Pageable page, Sport sport);
    List<Event> findAllBySport(Sport sport);
}
