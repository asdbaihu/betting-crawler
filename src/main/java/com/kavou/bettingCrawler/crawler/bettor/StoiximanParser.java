package com.kavou.bettingCrawler.crawler.bettor;

import com.kavou.bettingCrawler.crawler.interfaces.Parser;
import com.kavou.bettingCrawler.web.dao.*;
import com.kavou.bettingCrawler.web.entity.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

@Component
public class StoiximanParser implements Parser {

    // StoiximanParser constants
    private static final String BETTOR_NAME = "Stoiximan";
    private static final String INDEX_PAGE_URL = "https://www.stoiximan.gr/";

    // bettor variables
    private Bettor bettor;
    private String bettorName = getBettorName();

    // bet variables
    private Bet bet;
    private BigDecimal homeWin = null;
    private BigDecimal awayWin = null;

    // football bet variables
    private FootballBet footballBet;

    // lists to store the URL links
    private List<String> sportLinks = new ArrayList<>();
    private List<String> eventLinks = new ArrayList<>();
    private List<String> gameLinks = new ArrayList<>();

    // Jsoup connection parameter
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.81 Safari/537.36";

    // Dependency injections

    @Autowired
    private BettorRepository bettorRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private FootballBetRepository footballBetRepository;

    @Override
    // connect ang fetch the html page from the Url given in the constructor
    // stores the html page as a Document and returns it
    public Document connectAndFetchPage(String Url) {

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

    @Override
    public void fetchBettorData(Document doc) {

        // check (by bettor name) if bettor exists in database
        Bettor bettorExists = bettorRepository.getOneByName(bettorName);

        // if bettor does not exists in database
        if (bettorExists == null) {
            // create the object of bettor
            bettor = new Bettor(bettorName);
            // and save it to database
            saveEntity(bettor, bettorRepository);
        } else {
            bettor = bettorExists;
        }

    }

    @Override
    // extract the links for the sports (football, basketball, tennis, ...)
    // and store them to list
    public void fetchSportData(Document doc) {

        // get sport links and names

        // links
        String cssPathForSportLinks = ".pb.js-sportlist-toggle";
        Elements sportLinksAsElement = doc.select(cssPathForSportLinks);

        // names
        String cssPathForSportNames = ".pb.js-sportlist-toggle span.zt";
        Elements sportNamesAsElement = doc.select(cssPathForSportNames);

        // bettor object to check if associated with the sport
        Bettor bettorToCheck = bettor;

        // list with new sports
        // new sport is a sport that is not contained in the database (associated with "bettorToCheck")
        List<Sport> newSports = new ArrayList<>();

        // new sport
        Sport newSport;

        // sport name to check
        String sportName;

        for (int i=0; i<sportLinksAsElement.size(); i++) {

            // name of sport
            sportName = sportNamesAsElement.get(i).text();

            // check (by sport name and bettor) if sport exists in database
            boolean sportExists = sportRepository.existsByNameAndBettor(sportName, bettorToCheck);

            // if sport does not exists in database
            if (!sportExists) {
                // create it
                newSport = new Sport(sportName);
                // associate it with bettor
                newSport.setBettor(bettorToCheck);
                // add the sport to the new sports list
                newSports.add(newSport);
            }

            // link of sport
            String sportLink = sportLinksAsElement.get(i).absUrl("href");

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

        // if new sports exist
        if (!newSports.isEmpty()) {

            // associate the new sports with the bettor
            bettorToCheck.setSports(newSports);

            // update the bettor
            // new sports will also be saved
            saveEntity(bettorToCheck, betRepository);
        }

    }

    @Override
    // extract the links associated with events
    // Example: Sport is football, events are Premier League, World Cup, ...
    // and store them to list
    public void fetchEventData(Document doc) {

        // clear the list. The list must contain only the event links of the current sport
        eventLinks.clear();

        // get event links and names

        // names (we must create a string like: Αγγλία - Premier League)
        // first part of the name (regionHead)
        // Αγγλία, Eupora League, Βραζιλία
        String cssPathForRegionHead = ".js-region-head.a8x.a1l label";
        Elements regionHeadAsElement = doc.select(cssPathForRegionHead);

        // divs separated by region. Contains regionHead and regionBody
        // Αγγλία
        // - Premier League
        // - Championship
        // - EFL Cup ...
        String cssPathForRegion = ".js-region-body";
        Elements regionsAsElement = doc.select(cssPathForRegion);

        // sport name (to check if it exists in database by this name)
        String cssPathForSportName = ".a8k.w div.mb h3";
        Element sportNameAsElement = doc.select(cssPathForSportName).first();

        // name of sport to check if associated with the event
        String sportName = sportNameAsElement.text();

        // sport object to check if associated with the event
        Sport sportToCheck = sportRepository.getOneByName(sportName);

        // list with new events
        // new event is an event that is not contained in the database (associated with "sportToCheck")
        List<Event> newEvents = new ArrayList<>();

        // new event
        Event newEvent;

        // event name to check
        String eventName;

        // loop through regions
        for (int j=0; j<regionsAsElement.size(); j++) {

            // second part of the name as element (regionBody)
            // Premier League, Championship, EFL Cup, ...
            Elements childrenOfRegion = regionsAsElement.get(j).children();

            // regionHead as string
            String regionHead = regionHeadAsElement.get(j).text();

            // System.out.println("regionHead "+regionHead);

            // loop through region bodies
            for (int i=0; i<childrenOfRegion.size(); i++) {

                // second part of the name as string (regionBody)
                String regionBody = childrenOfRegion.get(i).text();

                // we check if the event name contains one of the words "Μακροχρόνια", "Νικητής", "Ειδικά στοιχήματα"
                // if it does not contains we add the event to database
                if (!(     regionBody.contains("Μακροχρόνια")
                        || regionBody.contains("Νικητής")
                        || regionBody.contains("Ειδικά")
                        || regionBody.contains("Προκριματικά"))
                        ) {

                    // System.out.println("regionBody "+regionBody);

                    // create the formatted name (event name)
                    // Αγγλία - Premier League, Αγγλία - Championship, Αγγλία - EFL Cup, ...
                    eventName = regionHead + " - " + regionBody;

                    // System.out.println(eventName);

                    // check (by event name and sport) if event exists in database
                    boolean eventExists = eventRepository.existsByNameAndSport(eventName, sportToCheck);

                    // if event does not exists in database
                    if (!eventExists) {
                        // create it
                        newEvent = new Event(eventName);
                        // associate it with the sport
                        newEvent.setSport(sportToCheck);
                        // add the event to the new events list
                        newEvents.add(newEvent);
                    }
                }
            }

        }

        // links
        String cssPathForEventLinks = ".js-region-body div.a90 a.a8t";
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

            // System.out.println("link is: "+eventLink);

            // we check if the event name contains one of the words "Μακροχρόνια", "Νικητής", "Ειδικά στοιχήματα"
            // if it does not contains we add the event to list
            String checkForLongTermBets = checkForLongTermBetsAsElement.get(i).text();
            if (!(     checkForLongTermBets.contains("Μακροχρόνια")
                    || checkForLongTermBets.contains("Νικητής")
                    || checkForLongTermBets.contains("Ειδικά")
                    || checkForLongTermBets.contains("Προκριματικά"))
                    ) {

                // System.out.println("Event link (Inside StoiximanParser): "+eventLink);

                /*
                ONLY
                FOR
                DEBUGGING
                */

                // if(eventLink.contains("I-liga-Poland-17396")) {
                eventLinks.add(eventLink);
                // }

            }

            // if (eventLinks.size() == 2) {
            //     break;
            // }

        }

        // if new events exist
        if (!newEvents.isEmpty()) {

            // associate the new events with the sport
            sportToCheck.setEvents(newEvents);

            // update the sport
            // new events will also be saved
            saveEntity(sportToCheck, sportRepository);
        }

    }

    @Override
    public void fetchGameData(Document doc) {

        // clear the list. The list must contain only the game links of the current event
        gameLinks.clear();

        // links
        String cssPathForGameLinks = ".js-event-click.a1i";
        Elements gameLinksAsElement = doc.select(cssPathForGameLinks);

        // data for creating game

        // Game constructor:
        // (int gameCode, String opponents, LocalDate date, LocalTime time)

        // match code
        // stoiximan does not support match code
        String cssPathForGameCode = "";
        int gameCode = 0;

        // opponents
        String cssPathForOpponents = ".js-event-click.a1i span";
        Elements opponentsAsElement = doc.select(cssPathForOpponents);

        // date and time
        String cssPathForDateAndTime = ".a93 span.a0r";
        Elements dateAsElement = doc.select(cssPathForDateAndTime);

        // data to check the database

        // event name
        String cssPathForEventName = ".js-league-title.mm label";
        Element eventNameAsElement = doc.select(cssPathForEventName).first();
        String eventName = eventNameAsElement.text();

        // sport object to check if associated with the event
        Event eventToCheck = eventRepository.getOneByName(eventName);

        // list with new games
        // new game is an game that is not contained in the database (associated with "eventToCheck")
        List<Game> newGames = new ArrayList<>();

        // new game
        Game newGame;

        // name of opponents to check
        String opponents;

        // date and time of the game
        LocalDate date;
        LocalTime time;

        for (int i=0; i<gameLinksAsElement.size(); i++) {

            String isLive = dateAsElement.get(i).text();

            // time must not be "LIVE"
            if (!isLive.equals("LIVE")) {

                // date and time as string
                // StoiximanParser sample date-time Format
                // 17/02 19:00
                String dateAsString = dateAsElement.get(i).text();

                Locale locale = new Locale("el", "GR");

                // we must add the year at the end of date-time

                LocalDateTime now = LocalDateTime.now();
                int defaultYear = now.getYear();

                DateTimeFormatter stringToDateFormatter = new DateTimeFormatterBuilder()
                        .appendPattern("dd/MM HH:mm")
                        .parseDefaulting(ChronoField.YEAR, defaultYear)
                        .toFormatter(locale);

                LocalDateTime dateTime = LocalDateTime.parse(dateAsString, stringToDateFormatter);

                date = dateTime.toLocalDate();
                time = dateTime.toLocalTime();

                // System.out.println("Inside StoiximanParser (date)): " + date);
                // System.out.println("Inside StoiximanParser (time)): " + time);

                // name of opponents
                opponents = opponentsAsElement.get(i).text();

                // System.out.println("Inside StoiximanParser - opponents: " + opponents);
                // System.out.println("Inside StoiximanParser - event to check: " + eventToCheck);
                // System.out.println("--------------------------------------");

                // check (by game name and event) if game exists in database
                boolean gameExists = gameRepository.existsByOpponentsAndEvent(opponents, eventToCheck);

                // if game does not exists in database
                if (!gameExists) {
                    // create it
                    newGame = new Game(gameCode, opponents, date, time);
                    // associate it with the event
                    newGame.setEvent(eventToCheck);
                    // add the game to the new games list
                    newGames.add(newGame);
                }

                // link of game
                String gameLink = gameLinksAsElement.get(i).absUrl("href");

                // add them to list
                gameLinks.add(gameLink);


            /*
            ONLY
            FOR
            DEBUGGING
             */
            // if (gameLinks.size() == 5) {
            //     break;
            // }
            }
        }

        // if new games exist
        if (!newGames.isEmpty()) {

            // associate the new games with the event
            eventToCheck.setGames(newGames);

            // update the event
            // new games will also be saved
            saveEntity(eventToCheck, eventRepository);
        }
    }

    @Override
    public void fetchBetData(Document doc) {

        // data for creating BETS
        // Bet constructor is:
        // (BigDecimal homeWin, BigDecimal awayWin)
        /*
        css path which contains [data-type='MRES'] or [data-type='MR12'] is for sports
        that has 3 different results (1, X, 2), like football, rugby, ...
        css path which contains [data-type='H2HT'] is for sports
        that has 2 different results (1, 2) like tennis, mma, basketball, ...
        */

        // get the name of the sport
        String cssPathForSportName = ".mb.a1s ul.a8p li a";
        // sport is the second element (i=1)
        Element sportNameAsElement = doc.select(cssPathForSportName).get(1);
        String sportName = sportNameAsElement.text();

        // home and away win
        String cssPathFor1X2 = "[data-type='MRES'] .ma.f.a39, [data-type='MR12'] .ma.f.a39, [data-type='H2HT'] .ma.f.a39";
        Elements oddsAsElement = doc.select(cssPathFor1X2);
        // home win is the first element
        String homeWinAsString = oddsAsElement.first().text();
        // away win is the last element
        String awayWinAsString = oddsAsElement.last().text();

        // Format bet data
        // convert String to BigDecimal (homeWin, awayWin, ...)
        homeWin = new BigDecimal(homeWinAsString);
        awayWin = new BigDecimal(awayWinAsString);

        // we get different bets for every sport so we call the appropriate method
        switch (sportName) {
            // football
            case "Ποδόσφαιρο":
                fetchFootballBet(doc);
        }

        // System.out.println("Inside StoiximanParser (home win): "+homeWinAsString);
        // System.out.println("Inside StoiximanParser (away win): "+awayWinAsString);

    }

    public void fetchFootballBet(Document doc) {

        // get the name of opponents of the bet
        String cssPathForOpponents = ".mb.a1s h3";
        Element opponentsAsElement = doc.select(cssPathForOpponents).first();
        String opponents = opponentsAsElement.text();

        // get the event of the bet
        String cssPathForEvent = ".a1s ul.a8p li a";
        Element eventAsElement = doc.select(cssPathForEvent).last();
        String eventAsString = eventAsElement.text();
        Event event = eventRepository.getOneByName(eventAsString);

        // get the game of the bet (by name and event)
        Game gameOfBet = null;
        try {
            gameOfBet = gameRepository.getOneByOpponentsAndEvent(opponents, event);
        } catch (Exception e) {
            throw new RuntimeException("Duplicate game name: "+ opponents +" to event: "+ eventAsString);
        }

        // print the game of bet
        // System.out.println(gameOfBet);

        // variables for creating FootballBet
        footballBet = new FootballBet();

        // set home and away win
        footballBet.setHomeWin(homeWin);
        footballBet.setAwayWin(awayWin);

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
        overGoals.put("over7_5", null);

        // list with under goals variables as String
        Map<String, BigDecimal> underGoals = new HashMap<>();
        underGoals.put("under0_5", null);
        underGoals.put("under1_5", null);
        underGoals.put("under2_5", null);
        underGoals.put("under3_5", null);
        underGoals.put("under4_5", null);
        underGoals.put("under5_5", null);
        underGoals.put("under6_5", null);
        underGoals.put("under7_5", null);

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
        String cssPathFor1X2 = "[data-type='MRES'] .ma.f.a39, [data-type='MR12'] .ma.f.a39";

        // 2) underOver (goals)
        String cssPathForGoalUnderOver = "[data-type='HCTG_0'] .ma.f.a39";
        String cssPathForGoalUnderOverLabel = "[data-type='HCTG_0'] .mb.a38";

        // 3) underOver extra (goals)
        String cssPathForGoalUnderOverExtra = "[data-type='HCTG_ADT'] .ma.f.a39";
        String cssPathForGoalUnderOverExtraLabel = "[data-type='HCTG_ADT'] .mb.a38";

        // 4) goal goal (both teams will score) - no goal
        String cssPathForGoalGoal = "[data-type='BTSC'] .ma.f.a39";

        // 5) double bet (1X, 2X, 12)
        String cssPathForDoubleBet = "[data-type='DBLC'] .ma.f.a39";

        // 6) half time - final time score (1/1, X/1, ...)
        String cssPathForHalfFinalScore = "[data-type='HTFT'] .ma.f.a39";

        // fetch football bets

        // 1) draw
        Elements odds1X2AsElement = doc.select(cssPathFor1X2);
        // draw is the middle (second) element of 3 elements
        // i=0 is the first, i=1 is the second, i=2 is the third
        String drawAsString = odds1X2AsElement.get(1).text();
        // System.out.println("Inside StoiximanParser (draw): "+drawAsString);

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
            StoiximanParser label format: Over 1.5
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
                case ("over7_5"):
                    footballBet.setOver7_5(oddToAdd);
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
                case ("under7_5"):
                    footballBet.setOver7_5(oddToAdd);
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

        // associate game with bets
        footballBet.setGame(gameOfBet);

        // save bet
        saveEntity(footballBet, footballBetRepository);

    }

    @Override
    public void saveEntity(Object theObject, JpaRepository theRepository) {

        // save the object (bettor or sport or event or ...)
        try {
            theRepository.save(theObject);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    // constructor
    public StoiximanParser() {
    }

    // getters and setters
    public static String getBettorName() {
        return BETTOR_NAME;
    }

    public void setBettorName(String bettorName) {
        this.bettorName = bettorName;
    }

    public BigDecimal getHomeWin() {
        return homeWin;
    }

    public void setHomeWin(BigDecimal homeWin) {
        this.homeWin = homeWin;
    }

    public BigDecimal getAwayWin() {
        return awayWin;
    }

    public void setAwayWin(BigDecimal awayWin) {
        this.awayWin = awayWin;
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

    public List<String> getGameLinks() {
        return gameLinks;
    }

    public void setGameLinks(List<String> gameLinks) {
        this.gameLinks = gameLinks;
    }

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public GameRepository getGameRepository() {
        return gameRepository;
    }

    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public static String getIndexPageUrl() {
        return INDEX_PAGE_URL;
    }

    public Bettor getBettor() {
        return bettor;
    }

    public void setBettor(Bettor bettor) {
        this.bettor = bettor;
    }

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }

    public FootballBet getFootballBet() {
        return footballBet;
    }

    public void setFootballBet(FootballBet footballBet) {
        this.footballBet = footballBet;
    }

    public BettorRepository getBettorRepository() {
        return bettorRepository;
    }

    public void setBettorRepository(BettorRepository bettorRepository) {
        this.bettorRepository = bettorRepository;
    }

    public SportRepository getSportRepository() {
        return sportRepository;
    }

    public void setSportRepository(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    public EventRepository getEventRepository() {
        return eventRepository;
    }

    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public BetRepository getBetRepository() {
        return betRepository;
    }

    public void setBetRepository(BetRepository betRepository) {
        this.betRepository = betRepository;
    }

    // toString
    @Override
    public String toString() {
        return "StoiximanParser{" +
                "BETTOR_NAME='" + BETTOR_NAME + '\'' +
                ", INDEX_PAGE_URL='" + INDEX_PAGE_URL + '\'' +
                '}';
    }
}
