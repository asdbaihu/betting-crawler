package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SportService {

    Sport getOneById(int sportId);
    Page<Sport> findAllByBettor(Pageable page, Bettor bettor);
}
