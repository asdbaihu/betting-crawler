package com.kavou.bettingCrawler.crawler.interfaces;

import org.jsoup.nodes.Document;

public interface Parser {

    Document connectAndFetchPage(String Url);
    void fetchSportLinks(Document doc);
    void fetchEventLinks(Document doc);
    void fetchGameLinks(Document doc);
    void fetchFinalData(Document doc);
    void saveFinalData();
}
