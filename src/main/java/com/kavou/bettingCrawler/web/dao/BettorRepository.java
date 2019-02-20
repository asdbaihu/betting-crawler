package com.kavou.bettingCrawler.web.dao;

import com.kavou.bettingCrawler.web.entity.Bettor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BettorRepository extends JpaRepository<Bettor, String> {

    Bettor getOneByName(String name);
    Bettor getOneById(int bettorId);
}
