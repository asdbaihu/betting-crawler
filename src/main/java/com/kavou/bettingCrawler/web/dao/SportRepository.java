package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Integer> {

    boolean existsByNameAndBettor(String name, Bettor bettor);
    Sport getOneByName(String name);
}
