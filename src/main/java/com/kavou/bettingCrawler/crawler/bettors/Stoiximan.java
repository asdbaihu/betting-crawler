package com.kavou.bettingCrawler.crawler.bettors;

import com.kavou.bettingCrawler.crawler.interfaces.Parser;
import com.kavou.bettingCrawler.web.api.entities.Bet;
import com.kavou.bettingCrawler.web.api.entities.Game;
import com.kavou.bettingCrawler.web.api.entities.betEntities.FootballBet;
import com.kavou.bettingCrawler.web.api.repositories.GameRepository;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Stoiximan implements Parser {

    private final String BETTOR_NAME = "Stoiximan";
    private final String INDEX_PAGE_URL = "https://www.stoiximan.gr/";

    private List<String> sportLinks = new ArrayList<>();
    private List<String> eventLinks = new ArrayList<>();
    private List<String> matchLinks = new ArrayList<>();
    private List<String> finalData = new ArrayList<>();

    // variables for creating Game object
    private int matchCode = 0;
    private String event = null;
    private String opponents = null;
    private Date date = null;
    private Date time = null;
    // associate Game with Bet
    private List<Bet> bets = new ArrayList<>();

    // variables for creating Bet object
    private BigDecimal homeWin = null;
    private BigDecimal awayWin = null;

    // common variables for game and bet objects
    private String sport = null;
    private String bettor = getBettorName();

    @Autowired
    private GameRepository gameRepository;

    @Override
    // connect ang fetch the html page from the Url given in the constructor
    // stores the html page as a Document and returns it
    public Document connectAndFetchPage(String Url) {

        Document document = null;
        try {
            // connect and store the html page to "document"
            document = Jsoup.connect(Url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    @Override
    // extract the links for the sports (football, basketball, tennis, ...)
    // and store them to list
    public void fetchSportLinks(Document doc) {

        String cssPathForSportLinks = "a.pb.js-sportlist-toggle";
        Elements sportLinksAsElement = doc.select(cssPathForSportLinks);

        for (Element sportLinkElement: sportLinksAsElement) {

            // link of sport
            String sportLink = sportLinkElement.absUrl("href");

            /*
            ONLY
            FOR
            DEBUGGING
             */
            // get football link ONLY
            if(sportLink.contains("Soccer-FOOT/")) {
                // add them to list
                sportLinks.add(sportLink);
            }
        }
    }

    @Override
    // extract the links for the events
    // Example: Sport is football, events are Premier League, World Cup, ...
    // and store them to list
    public void fetchEventLinks(Document doc) {

        // clear the list. The list must contain only the event links of the current sport
        eventLinks.clear();

        String cssPathForEventLinks = ".js-checkbox-category.js-league-class.a8p div.js-checkbox-category-body.js-league-class-body.a8t div.ar div.bx.fp div.js-checkbox-category.js-region.a8w div.js-checkbox-category-body.js-region-body div.a8y a.a8r";
        Elements eventLinksAsElement = doc.select(cssPathForEventLinks);

        /*
        we don't need the links for long term bets.
        example of long term bets:
        - the winner of a basketball championship
        - the winner of tennis tournament
        */
        Elements checkForLongTermBetsAsElement = doc.select(cssPathForEventLinks);

        for (int i=0; i<eventLinksAsElement.size(); i++) {

            // link of event
            String eventLink = eventLinksAsElement.get(i).absUrl("href");

            // we check if the event name contains one of the words "Μακροχρόνια", "Νικητής", "Ειδικά στοιχήματα"
            // if it does not contains we add the event to list
            String checkForLongTermBets = checkForLongTermBetsAsElement.get(i).text();
            if (    !( checkForLongTermBets.contains("Μακροχρόνια")
                    || checkForLongTermBets.contains("Νικητής"))
                    || checkForLongTermBets.contains("Ειδικά στοιχήματα")
               )
            {

                // System.out.println("Event link (Inside Stoiximan): "+eventLink);

                /*
                ONLY
                FOR
                DEBUGGING
                 */
                // get super league link ONLY
                // if(eventLink.contains("Super-League")) {
                // // add them to list
                eventLinks.add(eventLink);
                // }

                // if (eventLinks.size() == 10) {
                //     break;
                // }
            }
        }
    }

    @Override
    public void fetchGameLinks(Document doc) {

        // clear the list. The list must contain only the game links of the current event
        matchLinks.clear();

        String cssPathForMatchLinks = "td.akq.w a.js-event-click.a1g";
        Elements matchLinksAsElement = doc.select(cssPathForMatchLinks);

        String cssPathForCheckingIfLive = "td.akq.w div.a91 span.a0p";
        Elements checkingIfLiveAsElement = doc.select(cssPathForCheckingIfLive);

        for (int i=0; i<matchLinksAsElement.size(); i++) {

            // link of match
            String matchLink = matchLinksAsElement.get(i).absUrl("href");

            String isLive = checkingIfLiveAsElement.get(i).text();

            if (!isLive.equals("LIVE")) {
                // add them to list
                matchLinks.add(matchLink);
            }

            /*
            ONLY
            FOR
            DEBUGGING
             */
            // if (matchLinks.size() == 5) {
            //     break;
            // }
        }
    }

    @Override
    public void fetchFinalData(Document doc) {

        // data for game and bet
        String cssPathForSport = " div#js-layout-mainholder.c9.a1p div.a8i.w div.mb.a1q ul.a8n li a";

        // sport
        // sport name is second at list
        Element sportAsElement = doc.select(cssPathForSport).get(1);
        sport = sportAsElement.text();

        System.out.println("Inside Stoiximan (sport): "+sport);

        // data for creating GAME

        // Game constructor:
        // (String sport, String bettor, int matchCode, String event, String opponents, Date date, Date time)
        String cssPathForMatchCode = "";
        String cssPathForOpponents = "div.a8i.w div.mb.a1q h3";
        String cssPathForEvent = "div.a8i.w div.mb.a1q ul.a8n li a";
        String cssPathForDateAndTime = ".c9.a1p div.a8i.w div.ma.a8j div.a0p";

        // match code
        // stoiximan does not support match code
        matchCode = 0;

        // opponents
        Element opponentsAsElement = doc.select(cssPathForOpponents).first();
        opponents = opponentsAsElement.text();

        System.out.println("Inside Stoiximan (opponents): "+opponents);

        // event
        // event name is last at list
        Element eventAsElement = doc.select(cssPathForEvent).last();
        event = eventAsElement.text();

        System.out.println("Inside Stoiximan (event): "+event);

        // date and time
        Element dateAsElement = doc.select(cssPathForDateAndTime).first();
        String dateAsSting = dateAsElement.text();

        // System.out.println("Inside Stoiximan (date and time): "+date);

        // Format GAME data
        // convert String object to Date object (date and time)
        Locale locale = new Locale("el", "GR");
        SimpleDateFormat stringToDateFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", locale);
        try {
            date = stringToDateFormatter.parse(dateAsSting);
            time = stringToDateFormatter.parse(dateAsSting);
            // System.out.println("Date formatted (Date): "+dateFormatted);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        // convert Date object to String object
        // extracts date only
        SimpleDateFormat dateToStringFormatterDateOnly = new SimpleDateFormat("dd/MM/yyyy");
        String dateFormattedAsString = dateToStringFormatterDateOnly.format(date);
        // prints the date (format: dd/MM/yyyy)
        System.out.println("Inside Stoiximan (Date (String)): "+dateFormattedAsString);

        // extracts time only
        SimpleDateFormat dateToStringFormatterTimeOnly = new SimpleDateFormat("HH:mm");
        String timeFormattedAsString = dateToStringFormatterTimeOnly.format(date);
        // prints the time (format: HH:mm)
        System.out.println("Inside Stoiximan (Time (String)): "+timeFormattedAsString);

        // data for creating BETS

        // Bet constructor:
        // (String sport, String bettor, BigDecimal homeWin, BigDecimal awayWin)

        /*
        css path which contains [data-type='MRES'] or [data-type='MR12'] is for sports
        that has 3 different results (1, X, 2), like football, rugby, ...
        css path which contains [data-type='H2HT'] is for sports
        that has 2 different results (1, 2) like tennis, mma, basketball, ...
        */
        String cssPathFor1X2 = "[data-type='MRES'] .ma.f.a37, [data-type='MR12'] .ma.f.a37, [data-type='H2HT'] .ma.f.a37";

        Elements oddsAsElement = doc.select(cssPathFor1X2);

        // home win is the first element
        String homeWinAsString = oddsAsElement.first().text();

        // away win is the last element
        String awayWinAsString = oddsAsElement.last().text();

        // Format bet data
        // convert String to BigDecimal (homeWin, awayWin, ...)
        homeWin = new BigDecimal(homeWinAsString);
        awayWin = new BigDecimal(awayWinAsString);

        // we get different bets depending on sport so we call the appropriate method
        switch (sport) {
            // football
            case "Ποδόσφαιρο":
                fetchFootballBet(doc);
        }

        System.out.println("Inside Stoiximan (home win): "+homeWinAsString);
        System.out.println("Inside Stoiximan (away win): "+awayWinAsString);

    }

    public void fetchFootballBet(Document doc) {

        // clear bets list. The list must contain only the bets links of the current game
        bets.clear();

        // variables for creating FootballBet
        FootballBet footballBet = new FootballBet(sport, bettor, homeWin, awayWin);

        // draw
        BigDecimal draw;

        // list with over goals variables as String
        Map<String, BigDecimal> overGoals = new TreeMap<>();
        overGoals.put("over0_5", null);
        overGoals.put("over1_5", null);
        overGoals.put("over2_5", null);
        overGoals.put("over3_5", null);
        overGoals.put("over4_5", null);
        overGoals.put("over5_5", null);
        overGoals.put("over6_5", null);

        // list with under goals variables as String
        Map<String, BigDecimal> underGoals = new HashMap<>();
        underGoals.put("under0_5", null);
        underGoals.put("under1_5", null);
        underGoals.put("under2_5", null);
        underGoals.put("under3_5", null);
        underGoals.put("under4_5", null);
        underGoals.put("under5_5", null);
        underGoals.put("under6_5", null);

        // GG/NG
        BigDecimal goalGoal = null;
        BigDecimal noGoal = null;

        // double bet (1X, 2X, 12)
        BigDecimal homeDraw = null;
        BigDecimal awayDraw = null;
        BigDecimal homeAway = null;

        // half - final score (1/1, X/1, 2/1, ...)
        BigDecimal home_home = null;
        BigDecimal draw_home = null;
        BigDecimal away_home = null;
        BigDecimal home_draw = null;
        BigDecimal draw_draw = null;
        BigDecimal away_draw = null;
        BigDecimal home_away = null;
        BigDecimal draw_away = null;
        BigDecimal away_away = null;

        // 1) draw
        String cssPathFor1X2 = "[data-type='MRES'] .ma.f.a37, [data-type='MR12'] .ma.f.a37";

        // 2) underOver (goals)
        String cssPathForGoalUnderOver = "[data-type='HCTG_0'] .ma.f.a37";
        String cssPathForGoalUnderOverLabel = "[data-type='HCTG_0'] .mb.a36";

        // 3) underOver extra (goals)
        String cssPathForGoalUnderOverExtra = "[data-type='HCTG_ADT'] .ma.f.a37";
        String cssPathForGoalUnderOverExtraLabel = "[data-type='HCTG_ADT'] .mb.a36";

        // 4) goal goal (both teams will score) - no goal
        String cssPathForGoalGoal = "[data-type='BTSC'] .ma.f.a37";

        // 5) double bet (1X, 2X, 12)
        String cssPathForDoubleBet = "[data-type='DBLC'] .ma.f.a37";

        // 6) half time - final time score (1/1, X/1, ...)
        String cssPathForHalfFinalScore = "[data-type='HTFT'] .ma.f.a37";

        // fetch football bets

        // 1) draw
        Elements odds1X2AsElement = doc.select(cssPathFor1X2);
        // draw is the middle (second) element of 3 elements
        // i=0 is the first i=1 is the second, i=2 is the third
        String drawAsString = odds1X2AsElement.get(1).text();
        System.out.println("Inside Stoiximan (draw): "+drawAsString);

        // convert String to BigDecimal
        draw = new BigDecimal(drawAsString);

        // set draw
        footballBet.setDraw(draw);

        // 2) underOver (goals)
        // odds
        Elements goalUnderOverAsElement = doc.select(cssPathForGoalUnderOver);
        // labels
        Elements goalUnderOverLabelAsElement = doc.select(cssPathForGoalUnderOverLabel);

        for (int i=0; i<goalUnderOverAsElement.size(); i++) {

            // the label of the bet
            // Eg: Over 1.5
            String label = goalUnderOverLabelAsElement.get(i).text();

            // the odd of the bet
            // Eg: 2.5
            String odd = goalUnderOverAsElement.get(i).text();

            /*
            format the label to FootballBet object template
            Example:
            Stoiximan label format: Over 1.5
            FootballBet label format: over1_5
            */
            // lowercase the first letter
            // Over 1.5 -> over 1.5
            label = label.toLowerCase();

            // remove all blank characters. trims the word and removes blank characters between the words
            // over 1.5 -> over1.5
            label = label.replaceAll("\\s+","");

            // replace dot (.) with underscore (_)
            // over1.5 -> over1_5
            label = label.replace(".", "_");

            // label is now formatted in FootballBet template
            // System.out.println("formatted label: "+label);

            // store odd and label to the appropriate variable (depending on the label)
            // Eg label = over1_5, odd = 2,5
            // set over1_5 = 2,5 (convert the odd from String to BigDecimal)
            for (Map.Entry<String, BigDecimal> overMap: overGoals.entrySet()) {

                String overLabel = overMap.getKey();

                if (label.equals(overLabel)) {

                    BigDecimal overOdd = new BigDecimal(odd);
                    overMap.setValue(overOdd);

                    break;
                }
            }

            for (Map.Entry<String, BigDecimal> underMap: underGoals.entrySet()) {

                String underLabel = underMap.getKey();

                if (label.equals(underLabel)) {

                    BigDecimal underOdd = new BigDecimal(odd);
                    underMap.setValue(underOdd);

                    break;
                }
            }
        }

        // 3) underOver extra (goals)
        // fetch the data with the same method as 2) underOver (goals)
        // odds
        Elements goalUnderOverExtraAsElement = doc.select(cssPathForGoalUnderOverExtra);
        // labels
        Elements goalUnderOverLabelExtraAsElement = doc.select(cssPathForGoalUnderOverExtraLabel);

        for (int i=0; i<goalUnderOverExtraAsElement.size(); i++) {

            String label = goalUnderOverLabelExtraAsElement.get(i).text();
            String odd = goalUnderOverExtraAsElement.get(i).text();

            label = label.toLowerCase();

            label = label.replaceAll("\\s+","");

            label = label.replace(".", "_");

            // System.out.println("formatted label (extra): "+label);

            for (Map.Entry<String, BigDecimal> overMap: overGoals.entrySet()) {

                String overLabel = overMap.getKey();

                if (label.equals(overLabel)) {

                    BigDecimal overOdd = new BigDecimal(odd);
                    overMap.setValue(overOdd);

                    break;
                }
            }

            for (Map.Entry<String, BigDecimal> underMap: underGoals.entrySet()) {

                String underLabel = underMap.getKey();

                if (label.equals(underLabel)) {

                    BigDecimal underOdd = new BigDecimal(odd);
                    underMap.setValue(underOdd);

                    break;
                }
            }
        }

        for (Map.Entry<String, BigDecimal> overMap: overGoals.entrySet()) {

            String addTo = overMap.getKey();
            BigDecimal oddToAdd = overMap.getValue();

            switch (addTo) {
                case ("over0_5"):
                    footballBet.setOver0_5(oddToAdd);
                    break;
                case ("over1_5"):
                    footballBet.setOver1_5(oddToAdd);
                    break;
                case ("over2_5"):
                    footballBet.setOver2_5(oddToAdd);
                    break;
                case ("over3_5"):
                    footballBet.setOver3_5(oddToAdd);
                    break;
                case ("over4_5"):
                    footballBet.setOver4_5(oddToAdd);
                    break;
                case ("over5_5"):
                    footballBet.setOver5_5(oddToAdd);
                    break;
                case ("over6_5"):
                    footballBet.setOver6_5(oddToAdd);
                    break;
            }
        }

        for (Map.Entry<String, BigDecimal> underMap: underGoals.entrySet()) {

            String addTo = underMap.getKey();
            BigDecimal oddToAdd = underMap.getValue();

            switch (addTo) {
                case ("under0_5"):
                    footballBet.setUnder0_5(oddToAdd);
                    break;
                case ("under1_5"):
                    footballBet.setUnder1_5(oddToAdd);
                    break;
                case ("under2_5"):
                    footballBet.setUnder2_5(oddToAdd);
                    break;
                case ("under3_5"):
                    footballBet.setUnder3_5(oddToAdd);
                    break;
                case ("under4_5"):
                    footballBet.setUnder4_5(oddToAdd);
                    break;
                case ("under5_5"):
                    footballBet.setUnder5_5(oddToAdd);
                    break;
                case ("under6_5"):
                    footballBet.setUnder6_5(oddToAdd);
                    break;
            }
        }

        // some fields like GG/NG or double bet (1X, 2X, 12) may not exist in the page we are parsing

        // 4) goal goal - no goal
        Elements goalGoalNoGoalAsElement = doc.select(cssPathForGoalGoal);

        String goalGoalAsString = null;
        String noGoalAsString = null;

        // if the double bet elements exists in the page
        if(goalGoalNoGoalAsElement.size() > 0) {

            // goal goal odd is the first element (i=0)
            goalGoalAsString = doc.select(cssPathForGoalGoal).get(0).text();
            goalGoal = new BigDecimal(goalGoalAsString);

            // no goal odd is the last element (i=1)
            noGoalAsString = doc.select(cssPathForGoalGoal).get(1).text();
            noGoal = new BigDecimal(noGoalAsString);

        }

        // set BigDecimal values to football bet object
        footballBet.setGoalGoal(goalGoal);
        footballBet.setNoGoal(noGoal);

        // 5) double bet
        Elements doubleBetAsElements = doc.select(cssPathForDoubleBet);

        String homeDrawAsString = null;
        String awayDrawAsString = null;
        String homeAwayAsString = null;

        // if the double bet elements exists in the page
        if (doubleBetAsElements.size() > 0) {

            // 1X is the first element (i=0)
            homeDrawAsString = doubleBetAsElements.get(0).text();
            homeDraw = new BigDecimal(homeDrawAsString);

            // 2X is the second element (i=1)
            awayDrawAsString = doubleBetAsElements.get(1).text();
            awayDraw = new BigDecimal(awayDrawAsString);

            // 12 is the last element(i=2)
            homeAwayAsString = doubleBetAsElements.get(2).text();
            homeAway = new BigDecimal(homeAwayAsString);
        }

        // set BigDecimal values to football bet
        footballBet.setHomeDraw(homeDraw);
        footballBet.setAwayDraw(awayDraw);
        footballBet.setHomeAway(homeAway);

        // 6) half time - final time score
        Elements halfFinalScoreAsElements = doc.select(cssPathForHalfFinalScore);

        String home_homeAsString = null;
        String home_drawAsString = null;
        String home_awayAsString = null;
        String draw_homeAsString = null;
        String draw_drawAsString = null;
        String draw_awayAsString = null;
        String away_drawAsString = null;
        String away_homeAsString = null;
        String away_awayAsString = null;

        // if the half full time score elements exists in the page
        if (halfFinalScoreAsElements.size() > 0) {

            // 1/1 is the first element (i=0)
            home_homeAsString = doc.select(cssPathForHalfFinalScore).get(0).text();
            home_home = new BigDecimal(home_homeAsString);

            // 1/X is the second element (i=1)
            home_drawAsString = doc.select(cssPathForHalfFinalScore).get(1).text();
            home_draw = new BigDecimal(home_drawAsString);

            // 1/2 is the third element (i=2)
            home_awayAsString = doc.select(cssPathForHalfFinalScore).get(2).text();
            home_away = new BigDecimal(home_awayAsString);

            // X/1 is the fourth element (i=3)
            draw_homeAsString = doc.select(cssPathForHalfFinalScore).get(3).text();
            draw_home = new BigDecimal(draw_homeAsString);

            // X/X is the fifth element (i=4)
            draw_drawAsString= doc.select(cssPathForHalfFinalScore).get(4).text();
            draw_draw = new BigDecimal(draw_drawAsString);

            // X/2 is the sixth element (i=5)
            draw_awayAsString = doc.select(cssPathForHalfFinalScore).get(5).text();
            draw_away = new BigDecimal(draw_awayAsString);

            // 2/1 is the seventh element (i=6)
            away_homeAsString = doc.select(cssPathForHalfFinalScore).get(6).text();
            away_home = new BigDecimal(away_homeAsString);

            // 2/X is the eight element (i=7)
            away_drawAsString = doc.select(cssPathForHalfFinalScore).get(7).text();
            away_draw = new BigDecimal(away_drawAsString);

            // 2/2 is the last element (i=8)
            away_awayAsString = doc.select(cssPathForHalfFinalScore).get(8).text();
            away_away = new BigDecimal(away_awayAsString);

        }

        // set BigDecimal values to football bet
        footballBet.setHome_Home(home_home);
        footballBet.setHome_Draw(home_draw);
        footballBet.setHome_Away(home_away);
        footballBet.setAway_Home(away_home);
        footballBet.setDraw_Away(draw_away);
        footballBet.setDraw_Draw(draw_draw);
        footballBet.setDraw_Home(draw_home);
        footballBet.setAway_Away(away_away);
        footballBet.setAway_Draw(away_draw);

        // ALL data has been set to the football bet object

        // add football bets to bets list
        bets.add(footballBet);
    }

    @Override
    public void saveFinalData() {

        // create Game entity
        Game game = new Game(sport, bettor, matchCode, event, opponents , date, time);

        // associate Game with Bet
        game.setBets(bets);

        // save the game
        // Note: the (football, basketball, ...) bets will ALSO be saved
        try {
            gameRepository.save(game);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public Stoiximan() {
    }

    public String getBettorName() {
        return BETTOR_NAME;
    }

    public String getIndexPageUrl() {
        return INDEX_PAGE_URL;
    }

    public List<String> getSportLinks() {
        return sportLinks;
    }

    public void setSportLinks(List<String> sportLinks) {
        this.sportLinks = sportLinks;
    }

    public List<String> getEventLinks() {
        return eventLinks;
    }

    public void setEventLinks(List<String> eventLinks) {
        this.eventLinks = eventLinks;
    }

    public List<String> getMatchLinks() {
        return matchLinks;
    }

    public void setMatchLinks(List<String> matchLinks) {
        this.matchLinks = matchLinks;
    }

    public List<String> getFinalData() {
        return finalData;
    }

    public void setFinalData(List<String> finalData) {
        this.finalData = finalData;
    }

    @Override
    public String toString() {
        return "Stoiximan{" +
                "BETTOR_NAME='" + BETTOR_NAME + '\'' +
                ", INDEX_PAGE_URL='" + INDEX_PAGE_URL + '\'' +
                '}';
    }
}
