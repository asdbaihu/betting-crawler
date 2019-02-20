package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.BettorRepository;
import com.kavou.bettingCrawler.web.entity.Bettor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BettorServiceImpl implements BettorService{

    @Autowired
    private BettorRepository bettorRepository;

    @Override
    public Page<Bettor> findAll(Pageable page) {
        return bettorRepository.findAll(page);
    }

    @Override
    public Bettor getOneById(int bettorId) {
        return bettorRepository.getOneById(bettorId);
    }
}
