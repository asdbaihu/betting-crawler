package com.kavou.bettingCrawler.crawler.application;

import com.kavou.bettingCrawler.crawler.bettor.StoiximanParser;

import me.tongfei.progressbar.ProgressBar;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// @SpringBootApplication
// @ComponentScan("com.kavou")
// @EnableJpaRepositories("com.kavou")
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
        // get the bean for StoiximanParser
        StoiximanParser stoiximanParser = context.getBean(StoiximanParser.class);

        // give user the choice to crawl for data or stay in server mode

        System.out.println(ANSI_RED+"\nApplication is now running in server mode ... Press 1 to start extracting data from the web. "+ANSI_RESET);
        boolean crawlForData;
        Scanner in = new Scanner(System.in);
        int choice  = in.nextInt();

        if (choice == 1) { // Crawl for data
            crawlForData = true;
        } else { // Stay on server mode
            crawlForData = false;
        }

        if (crawlForData) {

            // crawling started
            float startTime = System.nanoTime();

            // list with the links of pages that did not loaded correctly
            List<String> pagesNotLoaded = new ArrayList<>();

            // list with the messages from exceptions
            List<String> exceptionMessages = new ArrayList<>();

            // crawl the webpages
            System.out.println(ANSI_GREEN + "\nCRAWLING STARTED" + ANSI_RESET);

            // URL of index page
            String indexPageUrl = stoiximanParser.getIndexPageUrl();
            // connect and fetch the index page
            Document indexDocument = stoiximanParser.connectAndFetchPage(indexPageUrl);

            // get bettor data
            try {
                stoiximanParser.fetchBettorData(indexDocument);
            } catch (Exception e) {
                exceptionMessages.add(e.getMessage());
            }

            // get sport data and create the sport links list
            try {
                stoiximanParser.fetchSportData(indexDocument);
            } catch (Exception e) {
                exceptionMessages.add(e.getMessage());
            }
            // URLs of sport links
            List<String> sportLinks = stoiximanParser.getSportLinks();

            // visit every sport link
            for (String sportLink : sportLinks) {

                Document sportDocument = stoiximanParser.connectAndFetchPage(sportLink);

                // create the event links list
                try {
                    stoiximanParser.fetchEventData(sportDocument);
                } catch (Exception e) {
                    pagesNotLoaded.add(sportLink);
                    exceptionMessages.add(e.getMessage());
                }

                // URLs of event links
                List<String> eventLinks = stoiximanParser.getEventLinks();

                // progress bar
                int max = eventLinks.size();
                try (ProgressBar bar = new ProgressBar("Fetching data for " + stoiximanParser.getSportName(), max)) {

                    // visit every event
                    for (String eventLink : eventLinks) {

                        bar.step();

                        // System.out.println("-----------> Event link: " + eventLink);

                        Document eventDocument = stoiximanParser.connectAndFetchPage(eventLink);

                        // create the game data
                        try {
                            stoiximanParser.fetchGameData(eventDocument);
                        } catch (Exception e) {
                            // e.printStackTrace();
                            pagesNotLoaded.add(eventLink);
                            exceptionMessages.add(e.getMessage());
                        }

                        // URLs of game links
                        List<String> gameLinks = stoiximanParser.getGameLinks();

                        // visit every match
                        for (String gameLink : gameLinks) {

                            // System.out.println("-----------> Game link: "+gameLink);

                            Document gameDocument = stoiximanParser.connectAndFetchPage(gameLink);

                            // fetch bets data
                            try {
                                stoiximanParser.fetchBetData(gameDocument);
                            } catch (Exception e) {
                                // e.printStackTrace();
                                pagesNotLoaded.add(gameLink);
                                exceptionMessages.add(e.getMessage());
                            }

                        }
                    }
                }
            }

            // if any page did not loaded correctly
            if (pagesNotLoaded.size() > 0) {
                System.out.println("");
                for (int i=0; i<pagesNotLoaded.size(); i++) {
                    System.out.println(ANSI_YELLOW + "Page: " + pagesNotLoaded.get(i) + " did not loaded correctly" + ANSI_RESET);
                    System.out.println(ANSI_GREEN + "Exception message: " + exceptionMessages.get(i) + ANSI_RESET);
                    System.out.println(ANSI_BLACK + "--------------------------------------" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_CYAN + "\nAll pages has been loaded correctly" + ANSI_RESET);
            }

            System.out.println("\nYou can now access your database to check the results!");
            System.out.println(ANSI_GREEN + "\nCRAWLING ENDED\n" + ANSI_RESET);

            // crawling finished
            float endTime = System.nanoTime();
            //Time in nanoseconds
            float totalTime = endTime - startTime;
            //Time in seconds
            double totalTimeInSec = totalTime / 1000000000.0;

            System.out.println(ANSI_PURPLE + "Total time running: " + ANSI_RESET + totalTimeInSec + " seconds");
            System.out.println(ANSI_PURPLE + "Total time running: " + ANSI_RESET + totalTimeInSec / 60 + " minutes");
        }
    }

    @Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }
}
