package com.kavou.bettingCrawler.crawler.applications;

import com.kavou.bettingCrawler.crawler.bettors.Stoiximan;
import me.tongfei.progressbar.ProgressBar;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@SpringBootApplication
@ComponentScan("com.kavou")
@EnableJpaRepositories("com.kavou")
public class StoiximanApp {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(StoiximanApp.class, args);
        // get the bean for Stoiximan
        Stoiximan stoiximan = context.getBean(Stoiximan.class);

        // crawl the webpages
        System.out.println("\n**************************** CRAWLING STARTED ****************************");

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
                System.out.println("\nPage of sport: "+sportLink+" did not loaded");
            }
            // URLs of event links
            List<String> eventLinks = stoiximan.getEventLinks();

            String sportToCrawl = stoiximan.getSport();
            System.out.println("\n-----------> Sport: "+sportToCrawl);

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
                        System.out.println("\nPage of event: "+eventLink+" did not loaded");
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
                            System.out.println("\nPage of game: "+gameLink+" did not loaded");
                        }

                        // save data to database
                        stoiximan.saveFinalData();

                    }


                }
            }
        }

        System.out.println("\n**************************** CRAWLING ENDED ****************************");

    }
}
