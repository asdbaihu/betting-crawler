package com.kavou.bettingCrawler.crawler.applications;

import com.kavou.bettingCrawler.crawler.bettors.Stoiximan;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan("com.kavou")
@EnableJpaRepositories("com.kavou")
public class StoiximanApp {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(StoiximanApp.class, args);
        // get the bean for Stoiximan
        Stoiximan stoiximan = context.getBean(Stoiximan.class);

        // list to store the links of the pages that did not loaded correctly
        List<String> pagesNotLoaded = new ArrayList<>();

        // crawl the webpages
        System.out.println(ANSI_BLUE+"\nCRAWLING STARTED"+ANSI_RESET);

        // URL of index page
        String indexPageUrl = stoiximan.getIndexPageUrl();
        // connect and fetch the index page
        Document indexDocument = stoiximan.connectAndFetchPage(indexPageUrl);

        // create the sport links list
        try {
            stoiximan.fetchSportLinks(indexDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // URLs of sport links
        List<String> sportLinks = stoiximan.getSportLinks();

        // visit every sport link
        for (String sportLink: sportLinks) {

            Document sportDocument = stoiximan.connectAndFetchPage(sportLink);

            // create the event links list
            try {
                stoiximan.fetchEventLinks(sportDocument);
            } catch (Exception e) {
                pagesNotLoaded.add(sportLink);
            }
            // URLs of event links
            List<String> eventLinks = stoiximan.getEventLinks();

            String sportToCrawl = stoiximan.getSport();
            System.out.println(ANSI_PURPLE+"\nExtracting data for: "+sportToCrawl+ANSI_RESET);
            System.out.println(ANSI_PURPLE+"----------------------------------------"+ANSI_RESET);

            // progress bar
            int max = eventLinks.size();
            try (ProgressBar bar = new ProgressBar(sportToCrawl+" progress", max)) {

                // visit every event
                for (String eventLink : eventLinks) {

                    bar.step();

                    // System.out.println("-----------> Event link: " + eventLink);

                    Document eventDocument = stoiximan.connectAndFetchPage(eventLink);

                    // create the game links list
                    try {
                        stoiximan.fetchGameLinks(eventDocument);
                    } catch (Exception e) {
                        pagesNotLoaded.add(eventLink);
                    }

                    // URLs of game links
                    List<String> gameLinks = stoiximan.getMatchLinks();

                    // visit every match
                    for (String gameLink : gameLinks) {

                        // System.out.println("-----------> Game link: "+gameLink);

                        Document gameDocument = stoiximan.connectAndFetchPage(gameLink);

                        // fetch final data (data for match and bets)
                        try {
                            stoiximan.fetchFinalData(gameDocument);
                        } catch (Exception e) {
                            pagesNotLoaded.add(gameLink);
                        }

                        // save data to database
                        stoiximan.saveFinalData();
                    }
                }
            }
        }

        // if any page did not loaded correctly
        if (pagesNotLoaded.size() > 0) {
            for (String page: pagesNotLoaded) {
                System.out.println(ANSI_YELLOW+"\nPage: "+page+" did not loaded correctly"+ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_CYAN+"\nAll pages has been loaded correctly"+ANSI_RESET);
        }

        System.out.println(ANSI_BLUE+"\nCRAWLING ENDED"+ANSI_RESET);
    }
}
