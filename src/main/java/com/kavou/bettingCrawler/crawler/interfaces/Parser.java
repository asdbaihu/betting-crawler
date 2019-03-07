package com.kavou.bettingCrawler.crawler.interfaces;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;

public interface Parser {


    // Jsoup connection parameter
    String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.81 Safari/537.36";

    // default method, no need to implement this method
    // connect and store the html webpage to a document variable
    default Document connectAndFetchPage(String Url) {

        Document document = null;
        try {
            // connect and store the html page to "document"
            document = Jsoup.connect(Url)
                    .userAgent(USER_AGENT)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }
    void fetchBettorData(Document doc);
    void fetchSportData(Document doc);
    void fetchEventData(Document doc);
    void fetchGameData(Document doc);
    void fetchBetData(Document doc);
    void saveEntity(Object entity, JpaRepository<Object, Integer> repository);
}
