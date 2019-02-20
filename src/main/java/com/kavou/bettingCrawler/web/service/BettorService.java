package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.entity.Bettor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BettorService {

    Page<Bettor> findAll(Pageable page);
    Bettor getOneById(int bettorId);
}
