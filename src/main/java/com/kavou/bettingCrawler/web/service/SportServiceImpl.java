package com.kavou.bettingCrawler.web.service;

import com.kavou.bettingCrawler.web.dao.SportRepository;
import com.kavou.bettingCrawler.web.entity.Bettor;
import com.kavou.bettingCrawler.web.entity.Sport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SportServiceImpl implements SportService {

    @Autowired
    private SportRepository sportRepository;

    @Override
    public Sport getOneById(int sportId) {
        return sportRepository.getOneById(sportId);
    }

    @Override
    public Page<Sport> findAllByBettor(Pageable page, Bettor bettor) {
        return sportRepository.findAllByBettor(page, bettor);
    }

}
