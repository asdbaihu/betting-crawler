package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BettorRepository extends JpaRepository<Bettor, String> {

    Bettor getOneByName(String name);
}
