package com.kavou.bettingCrawler.crawler.applications;

import com.kavou.bettingCrawler.crawler.bettors.Stoiximan;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@ComponentScan("com.kavou")
@EnableJpaRepositories("com.kavou")
public class StoiximanApp {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(StoiximanApp.class, args);

        // get the bean for Stoiximan
        Stoiximan stoiximan = context.getBean(Stoiximan.class);

        // URL of index page
        String indexPageUrl = stoiximan.getIndexPageUrl();
        // connect and fetch the index page
        Document indexDocument = stoiximan.connectAndFetchPage(indexPageUrl);

        // create the sport links list
        stoiximan.fetchSportLinks(indexDocument);
        // URLs of sport links
        List<String> sportLinks = stoiximan.getSportLinks();

        // visit every sport link
        for (String sportLink: sportLinks) {

            System.out.println("------------>>>>>>>>>> Sport link: "+sportLink);

            Document sportDocument = stoiximan.connectAndFetchPage(sportLink);

            // create the event links list
            stoiximan.fetchEventLinks(sportDocument);
            // URLs of event links
            List<String> eventLinks = stoiximan.getEventLinks();

            // visit every event
            for (String eventLink: eventLinks) {

                System.out.println("------------>>>>>>>>>> Event link: "+eventLink);

                Document eventDocument = stoiximan.connectAndFetchPage(eventLink);

                // create the game links list
                stoiximan.fetchGameLinks(eventDocument);
                // URLs of game links
                List<String> gameLinks = stoiximan.getMatchLinks();

                // for (String m: matchLinks) {
                //     System.out.println("------------>>>>>>>>>> Match Links: "+m);
                // }

                // visit every match
                for (String gameLink: gameLinks) {

                    System.out.println("------------>>>>>>>>>> Game link: "+gameLink);

                    Document matchDocument = stoiximan.connectAndFetchPage(gameLink);

                    // fetch final data (data for match and bets)
                    stoiximan.fetchFinalData(matchDocument);

                    // save data to database
                    stoiximan.saveFinalData();
                }
            }
        }
        System.out.println("**************************** END ****************************");
    }
}
