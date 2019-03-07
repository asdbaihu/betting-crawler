package com.kavou.bettingCrawler.crawler.bettor;

import com.kavou.bettingCrawler.crawler.interfaces.Parser;
import com.kavou.bettingCrawler.web.dao.*;
import com.kavou.bettingCrawler.web.entity.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

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

    // Stoiximan constants
    private static final String BETTOR_NAME = "Stoiximan";
    private static final String INDEX_PAGE_URL = "https://www.stoiximan.gr/";

    // bettor variables
    private Bettor bettor;
    private String bettorName = getBettorName();

    // sport name we are crawling
    private String sportName;

    // game of the bet we are crawling
    private Game gameOfBet;

    // bet variables
    private Bet bet;
    private BigDecimal homeWin = null;
    private BigDecimal awayWin = null;

    // lists to store the URL links
    private List<String> sportLinks = new ArrayList<>();
    private List<String> eventLinks = new ArrayList<>();
    private List<String> gameLinks = new ArrayList<>();

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
        String sportNameToCheck;

        for (int i=0; i<sportLinksAsElement.size(); i++) {

            // name of sport
            sportNameToCheck = sportNamesAsElement.get(i).text();

            // check (by sport name and bettor) if sport exists in database
            boolean sportExists = sportRepository.existsByNameAndBettor(sportNameToCheck, bettorToCheck);

            // if sport does not exists in database
            if (!sportExists) {
                // create it
                newSport = new Sport(sportNameToCheck);
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
            // if(sportLink.contains("Soccer-FOOT/")) {
            // if(sportLink.contains("Basketball-BASK/")) {
            // if(sportLink.contains("Tennis-TENN/")) {
            if(sportLink.contains("Soccer-FOOT/") || sportLink.contains("Basketball-BASK/") || sportLink.contains("Tennis-TENN/")) {
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
        sportName = sportNameAsElement.text();

        // sport object to check if associated with the event
        Sport sportToCheck = sportRepository.getOneByNameAndBettor(sportName, bettor);

        // list with new events
        // new event is an event that is not contained in the database (associated with "sportToCheck")
        List<Event> newEvents = new ArrayList<>();

        // new event
        Event newEvent;

        // event name to check
        String eventName;

        // loop through regions
        for (int j=0; j<regionsAsElement.size(); j++) {

            // regionHead as string
            String regionHead = regionHeadAsElement.get(j).text();

            // System.out.println("regionHead "+regionHead);

            // second part of the name as element (regionBody)
            // Premier League, Championship, EFL Cup, ...
            Elements childrenOfRegion = regionsAsElement.get(j).children();

            // loop through region bodies
            for (int i=0; i<childrenOfRegion.size(); i++) {

                // second part of the name as string (regionBody)
                String regionBody = childrenOfRegion.get(i).text();

                // we check if the event name contains one of the words "Μακροχρόνια", "Νικητής", "Ειδικά στοιχήματα"
                // if it does not contains we add the event to database
                if (!(     regionBody.contains("Μακροχρόνια")
                        || regionBody.contains("Νικητής")
                        || regionBody.contains("Ειδικά")
                        || regionBody.contains("Προκριματικά")
                        // Basketball
                        || regionBody.contains("Πρόκριση")
                        || regionBody.contains("Είσοδος")
                        || regionBody.contains("Τερματισμός")
                        || regionBody.contains("τελικό")
                        || regionBody.contains("Σκορ"))
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
                    || checkForLongTermBets.contains("Προκριματικά")
                    // Basketball
                    || checkForLongTermBets.contains("Πρόκριση")
                    || checkForLongTermBets.contains("Είσοδος")
                    || checkForLongTermBets.contains("Τερματισμός")
                    || checkForLongTermBets.contains("τελικό")
                    || checkForLongTermBets.contains("Σκορ"))
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

            // if (eventLinks.size() == 5) {
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

        // sport
        String cssPathForSportName = ".a8k.w div.mb h3";
        String sportName = doc.select(cssPathForSportName).text();
        Sport sportToCheck = sportRepository.getOneByNameAndBettor(sportName, bettor);

        // sport object to check if associated with the event
        Event eventToCheck = eventRepository.getOneByNameAndSport(eventName, sportToCheck);

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

        homeWin = null;
        awayWin = null;

        // data for creating BETS
        // Bet constructor is:
        // (BigDecimal homeWin, BigDecimal awayWin)
        /*
        css path which contains [data-type='MRES'] or [data-type='MR12'] is for sports
        that has 3 different results (1, X, 2), like football, rugby, ...
        css path which contains [data-type='H2HT'] or [data-type='HTOH_0'] is for sports
        that has 2 different results (1, 2) like tennis, mma, basketball, ...
        */
        // home and away win
        String cssPathFor1X2 = " [data-type='MRES'] .ma.f.a39, [data-type='MR12'] .ma.f.a39, " +
                               " [data-type='H2HT'] .ma.f.a39, [data-type='HTOH_0'] .ma.f.a39";
        Elements oddsAsElement = doc.select(cssPathFor1X2);

        // get the name of opponents of the bet
        String cssPathForOpponents = ".mb.a1s h3";
        Element opponentsAsElement = doc.select(cssPathForOpponents).first();
        String opponents = opponentsAsElement.text();

        // get the event of the bet (by name and sport)
        String cssPathForEvent = ".a1s ul.a8p li a";
        Element eventAsElement = doc.select(cssPathForEvent).last();
        String eventAsString = eventAsElement.text();

        Sport sportOfEvent = sportRepository.getOneByNameAndBettor(sportName, bettor);

        Event event = eventRepository.getOneByNameAndSport(eventAsString, sportOfEvent);

        // get the game of the bet (by name and event)
        try {
            gameOfBet = gameRepository.getOneByOpponentsAndEvent(opponents, event);
        } catch (Exception e) {
            throw new RuntimeException("Duplicate game name: "+ opponents +" to event: "+ eventAsString);
        }

        // print the game of bet
        // System.out.println(gameOfBet);

        if (oddsAsElement.size() > 0) {

            // home win is the first element
            String homeWinAsString = oddsAsElement.first().text();
            // away win is the last element
            String awayWinAsString = oddsAsElement.last().text();

            // Format bet data
            // convert String to BigDecimal (homeWin, awayWin, ...)
            homeWin = new BigDecimal(homeWinAsString);
            awayWin = new BigDecimal(awayWinAsString);
        }

        // System.out.println("Inside StoiximanParser (home win): "+homeWin);
        // System.out.println("Inside StoiximanParser (away win): "+awayWin);

        // we get different bets for every sport so we call the appropriate method
        switch (sportName) {
            // football
            case "Ποδόσφαιρο":
                fetchFootballBet(doc);
                break;
            case "Μπάσκετ":
                fetchBasketballBet(doc);
                break;
            case "Τένις":
                fetchTennisBet(doc);
                break;
        }

    }

    public void fetchFootballBet(Document doc) {

        // variables for creating FootballBet
        FootballBet footballBet = new FootballBet();

        // set home and away win
        footballBet.setHomeWin(homeWin);
        footballBet.setAwayWin(awayWin);

        // draw
        BigDecimal draw;

        // under/over (under 0.5, over 0.5, ...)
        BigDecimal underOverOdd = null;

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

        // CSS paths

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
            String oddAsString = goalUnderOverAsElement.get(i).text();

            // convert odd to BigDecimal
            underOverOdd = new BigDecimal(oddAsString);

            // set the value to the appropriate variable depending on label
            // eg: if label="Over 1.5" and odd=2,5 then execute -> footballBet.setOver1_5(odd);
            switch (label) {
                case ("Over 0.5"):
                    footballBet.setOver0_5(underOverOdd);
                    break;
                case ("Over 1.5"):
                    footballBet.setOver1_5(underOverOdd);
                    break;
                case ("Over 2.5"):
                    footballBet.setOver2_5(underOverOdd);
                    break;
                case ("Over 3.5"):
                    footballBet.setOver3_5(underOverOdd);
                    break;
                case ("Over 4.5"):
                    footballBet.setOver4_5(underOverOdd);
                    break;
                case ("Over 5.5"):
                    footballBet.setOver5_5(underOverOdd);
                    break;
                case ("Over 6.5"):
                    footballBet.setOver6_5(underOverOdd);
                    break;
                case ("Over 7.5"):
                    footballBet.setOver7_5(underOverOdd);
                    break;
                case ("Under 0.5"):
                    footballBet.setUnder0_5(underOverOdd);
                    break;
                case ("Under 1.5"):
                    footballBet.setUnder1_5(underOverOdd);
                    break;
                case ("Under 2.5"):
                    footballBet.setUnder2_5(underOverOdd);
                    break;
                case ("Under 3.5"):
                    footballBet.setUnder3_5(underOverOdd);
                    break;
                case ("Under 4.5"):
                    footballBet.setUnder4_5(underOverOdd);
                    break;
                case ("Under 5.5"):
                    footballBet.setUnder5_5(underOverOdd);
                    break;
                case ("Under 6.5"):
                    footballBet.setUnder6_5(underOverOdd);
                    break;
                case ("Under 7.5"):
                    footballBet.setUnder7_5(underOverOdd);
                    break;
            }
        }

        // 3) underOver extra (goals)
        // fetch the data with the same method as 2) underOver (goals)
        // odds
        Elements goalUnderOverExtraAsElement = doc.select(cssPathForGoalUnderOverExtra);
        // labels
        Elements goalUnderOverLabelExtraAsElement = doc.select(cssPathForGoalUnderOverExtraLabel);

        for (int i=0; i<goalUnderOverExtraAsElement.size(); i++) {

            // the label of the bet
            // Eg: Over 1.5
            String label = goalUnderOverLabelExtraAsElement.get(i).text();

            // the odd of the bet
            // Eg: 2.5
            String oddAsString = goalUnderOverExtraAsElement.get(i).text();

            // convert odd to BigDecimal
            underOverOdd = new BigDecimal(oddAsString);

            // set the value to the appropriate variable depending on label
            // eg: if label="Over 1.5" and odd=2,5 then execute -> footballBet.setOver1_5(odd);
            switch (label) {
                case ("Over 0.5"):
                    footballBet.setOver0_5(underOverOdd);
                    break;
                case ("Over 1.5"):
                    footballBet.setOver1_5(underOverOdd);
                    break;
                case ("Over 2.5"):
                    footballBet.setOver2_5(underOverOdd);
                    break;
                case ("Over 3.5"):
                    footballBet.setOver3_5(underOverOdd);
                    break;
                case ("Over 4.5"):
                    footballBet.setOver4_5(underOverOdd);
                    break;
                case ("Over 5.5"):
                    footballBet.setOver5_5(underOverOdd);
                    break;
                case ("Over 6.5"):
                    footballBet.setOver6_5(underOverOdd);
                    break;
                case ("Over 7.5"):
                    footballBet.setOver7_5(underOverOdd);
                    break;
                case ("Under 0.5"):
                    footballBet.setUnder0_5(underOverOdd);
                    break;
                case ("Under 1.5"):
                    footballBet.setUnder1_5(underOverOdd);
                    break;
                case ("Under 2.5"):
                    footballBet.setUnder2_5(underOverOdd);
                    break;
                case ("Under 3.5"):
                    footballBet.setUnder3_5(underOverOdd);
                    break;
                case ("Under 4.5"):
                    footballBet.setUnder4_5(underOverOdd);
                    break;
                case ("Under 5.5"):
                    footballBet.setUnder5_5(underOverOdd);
                    break;
                case ("Under 6.5"):
                    footballBet.setUnder6_5(underOverOdd);
                    break;
                case ("Under 7.5"):
                    footballBet.setUnder7_5(underOverOdd);
                    break;
            }
        }

        // some fields like GG/NG or double bet (1X, 2X, 12) may not exist in the page we are parsing

        // 4) goal goal - no goal
        Elements goalGoalNoGoalAsElement = doc.select(cssPathForGoalGoal);

        String goalGoalAsString;
        String noGoalAsString;

        // if the double bet elements exists in the page
        if (goalGoalNoGoalAsElement.size() > 0) {

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

        String homeDrawAsString;
        String awayDrawAsString;
        String homeAwayAsString;

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

        String home_homeAsString;
        String home_drawAsString;
        String home_awayAsString;
        String draw_homeAsString;
        String draw_drawAsString;
        String draw_awayAsString;
        String away_drawAsString;
        String away_homeAsString;
        String away_awayAsString;

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
        saveEntity(footballBet, betRepository);

    }

    public void fetchBasketballBet (Document doc) {

        // variables for creating FootballBet
        BasketballBet basketballBet = new BasketballBet();

        // set home and away win
        basketballBet.setHomeWin(homeWin);
        basketballBet.setAwayWin(awayWin);

        // draw
        BigDecimal draw = null;

        // half - final score (1/1, X/1, 2/1, ...)
        BigDecimal home_home = null;
        BigDecimal draw_home = null;
        BigDecimal away_home = null;
        BigDecimal home_away = null;
        BigDecimal draw_away = null;
        BigDecimal away_away = null;

        // CSS paths

        // 1) draw
        String cssPathFor1X2 = "[data-type='MRES'] .ma.f.a39, [data-type='MR12'] .ma.f.a39";

        // 2) half time - final time score (1/1, X/1, ...)
        String cssPathForHalfFinalScore = "[data-type='HTOT'] .ma.f.a39";

        // fetch basketballBet bets

        // 1) draw
        Elements odds1X2AsElement = doc.select(cssPathFor1X2);
        String drawAsString;

        // check if there is odd for draw (draw exists only in Champions League event)
        if (odds1X2AsElement.size() > 0) {
            // draw is the middle (second) element of 3 elements
            // i=0 is the first, i=1 is the second, i=2 is the third
            drawAsString = odds1X2AsElement.get(1).text();

            // convert String to BigDecimal
            draw = new BigDecimal(drawAsString);
        }
        // System.out.println("Inside StoiximanParser (draw): "+drawAsString);

        // set draw
        basketballBet.setDraw(draw);

        // 2) half time - final time score
        Elements halfFinalScoreAsElements = doc.select(cssPathForHalfFinalScore);

        String home_homeAsString;
        String home_awayAsString;
        String draw_homeAsString;
        String draw_awayAsString;
        String away_homeAsString;
        String away_awayAsString;

        // if the half full time score elements exists in the page
        if (halfFinalScoreAsElements.size() > 0) {

            // 1/1 is the first element (i=0)
            home_homeAsString = doc.select(cssPathForHalfFinalScore).get(0).text();
            home_home = new BigDecimal(home_homeAsString);

            // X/1 is the second element (i=1)
            draw_homeAsString = doc.select(cssPathForHalfFinalScore).get(1).text();
            draw_home = new BigDecimal(draw_homeAsString);

            // 2/1 is the third element (i=2)
            away_homeAsString = doc.select(cssPathForHalfFinalScore).get(2).text();
            away_home = new BigDecimal(away_homeAsString);

            // 1/2 is the fourth element (i=3)
            home_awayAsString = doc.select(cssPathForHalfFinalScore).get(3).text();
            home_away = new BigDecimal(home_awayAsString);

            // X/2 is the sixth element (i=4)
            draw_awayAsString = doc.select(cssPathForHalfFinalScore).get(4).text();
            draw_away = new BigDecimal(draw_awayAsString);

            // 2/2 is the last element (i=5
            away_awayAsString = doc.select(cssPathForHalfFinalScore).get(5).text();
            away_away = new BigDecimal(away_awayAsString);

        }

        // set BigDecimal values to football bet
        basketballBet.setHome_Home(home_home);
        basketballBet.setHome_Away(home_away);
        basketballBet.setAway_Home(away_home);
        basketballBet.setDraw_Away(draw_away);
        basketballBet.setDraw_Home(draw_home);
        basketballBet.setAway_Away(away_away);

        // ALL data has been set to the football bet object

        // associate game with bets
        basketballBet.setGame(gameOfBet);

        // save bet
        saveEntity(basketballBet, betRepository);

    }

    public void fetchTennisBet(Document doc) {

        // variables for creating FootballBet
        TennisBet tennisBet = new TennisBet();

        // home winner set 1
        BigDecimal homeWinnerSet1 = null;
        // away winner set 1
        BigDecimal awayWinnerSet1 = null;

        // 2-0 set score
        BigDecimal Two_Zero = null;
        // 0-2 set score
        BigDecimal Zero_Two = null;
        // 2-1 set score
        BigDecimal Two_One = null;
        // 1-2 set score
        BigDecimal One_Two = null;

        // set home and away win
        tennisBet.setHomeWin(homeWin);
        tennisBet.setAwayWin(awayWin);

        // CSS paths

        // 1) winner of set 1
        String cssPathForWinnerSet1 = "[data-type='STWN_1'] .ma.f.a39";

        // 2) set score (2-0, 2-1, 0-2, 1-2)
        String cssPathForSetScore = "[data-type='BTOF'] .ma.f.a39";

        // fetch tennis bets

        // 1) winner of set 1
        Elements winnerSet1AsElement = doc.select(cssPathForWinnerSet1);

        // if bets exists
        if (winnerSet1AsElement.size() > 0) {

            // home player wins set 1
            String homeWinnerAsString = winnerSet1AsElement.get(0).text();
            // System.out.println("Inside StoiximanParser (homeWinnerAsString): "+homeWinnerAsString);

            // away player wins set 1
            String awayWinnerAsString = winnerSet1AsElement.get(1).text();
            // System.out.println("Inside StoiximanParser (awayWinnerAsString): "+awayWinnerAsString);

            // convert String to BigDecimal
            homeWinnerSet1= new BigDecimal(homeWinnerAsString);
            awayWinnerSet1= new BigDecimal(awayWinnerAsString);
        }

        // set home winner at set 1
        tennisBet.setHomeWinnerSet1(homeWinnerSet1);
        // set away winner at set 1
        tennisBet.setAwayWinnerSet1(awayWinnerSet1);

        // 2) set score
        Elements setScoreAsElement = doc.select(cssPathForSetScore);

        // if bets exists
        if (setScoreAsElement.size() > 0) {

            // 2-0 score
            String Two_Zero_asString = setScoreAsElement.get(0).text();
            // System.out.println("Inside StoiximanParser (Two_Zero_asString): "+Two_Zero_asString);

            // 2-1 score
            String Two_One_asString = setScoreAsElement.get(1).text();
            // System.out.println("Inside StoiximanParser (Two_One_asString): "+Two_One_asString);

            // 0-2 score
            String Zero_Two_asString = setScoreAsElement.get(2).text();
            // System.out.println("Inside StoiximanParser (Zero_Two_asString): "+Zero_Two_asString);

            // 1-2
            String One_Two_asString = setScoreAsElement.get(3).text();
            // System.out.println("Inside StoiximanParser (One_Two_asString): "+One_Two_asString);

            // convert String to BigDecimal
            Two_Zero= new BigDecimal(Two_Zero_asString);
            Zero_Two = new BigDecimal(Zero_Two_asString);
            Two_One= new BigDecimal(Two_One_asString);
            One_Two = new BigDecimal(One_Two_asString);

        }

        // set 2-0
        tennisBet.setTwo_Zero(Two_Zero);
        // set 0-2
        tennisBet.setZero_Two(Zero_Two);
        // set 2-1
        tennisBet.setTwo_One(Two_One);
        // set 1-2
        tennisBet.setOne_Two(One_Two);

        // ALL data has been set to the tennis bet object

        // associate game with bets
        tennisBet.setGame(gameOfBet);

        // save bet
        saveEntity(tennisBet, betRepository);

    }

    @Override
    public void saveEntity(Object entity, JpaRepository repository) {

        // save the object (bettor or sport or event or ...)
        try {
            repository.save(entity);
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

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    @Override
    public String toString() {
        return "StoiximanParser{" +
                "BETTOR_NAME='" + BETTOR_NAME + '\'' +
                ", INDEX_PAGE_URL='" + INDEX_PAGE_URL + '\'' +
                '}';
    }
}
