package com.kavou.bettingCrawler.crawler.interfaces;

import org.jsoup.nodes.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Parser {

    Document connectAndFetchPage(String Url);
    void fetchBettorData(Document doc);
    void fetchSportData(Document doc);
    void fetchEventData(Document doc);
    void fetchGameData(Document doc);
    void fetchBetData(Document doc);
    void saveEntity(Object entity, JpaRepository<Object, Integer> repository);
}
