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
import java.util.*;

@Component
public class SportingbetParser implements Parser {

    // Sportingbet constants
    private static final String BETTOR_NAME = "Sportingbet";
    private static final String INDEX_PAGE_URL = "https://sports.sportingbet.gr";

    // bettor variables
    private Bettor bettor;
    private String bettorName = getBettorName();

    // sport name we are crawling
    private String sportName;

    // event name we are crawling
    private String eventName;

    // opponents name we are crawling
    private String opponents;

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
    public void fetchSportData(Document doc) {

        // get sport links and names

        // links
        String cssPathForSportLinks = ".sports-links__link.sports-links__link--all-sports";
        Elements sportLinksAsElement = doc.select(cssPathForSportLinks);

        // names
        String cssPathForSportNames = ".sports-links__link.sports-links__link--all-sports span.sports-links__name";
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

            // System.out.println("Sport link: "+sportLink);
            // System.out.println("Sport name: "+sportNameToCheck);

            /*
            ONLY
            FOR
            DEBUGGING
             */
            // football
            if(sportLink.contains("/4/")) {
            // basketball
            // if(sportLink.contains("/7/")) {
            // if(sportLink.contains("Tennis-TENN/")) {
            // if(sportLink.contains("Soccer-FOOT/") || sportLink.contains("Basketball-BASK/") || sportLink.contains("Tennis-TENN/")) {
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
    public void fetchEventData(Document doc) {

        // clear the list. The list must contain only the event links of the current sport
        eventLinks.clear();

        // get event links and names


        // get event links and names

        // names (we must create a string like: Αγγλία - Premier League)
        // first part of the name (regionHead)
        // Αγγλία, Eupora League, Βραζιλία
        String cssPathForRegionHead = ".nav-region-name.sports-links__text," +
                                      ".nav-special-name.sports-links__text";
        Elements regionHeadAsElement = doc.select(cssPathForRegionHead);

        // divs separated by region. Contains regionHead and regionBody
        // Αγγλία
        // - Premier League
        // - Championship
        // - EFL Cup ...
        String cssPathForRegion = "#nav-special-leagues ul.sports-links.hide, " +
                                  "#nav-top-list ul.sports-links.hide, " +
                                  "#nav-more-list ul.sports-links.hide";
        Elements regionsAsElement = doc.select(cssPathForRegion);

        // sport name (to check if it exists in database by this name)
        String cssPathForSportName = "#nav-sport-name.sports-links__text";
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
                // contains number and name of region (13 Κύπελλο Αγγλίας)
                String regionBody = childrenOfRegion.get(i).text();

                // we must keep only the name of region (Κύπελλο Αγγλίας)
                // we split th
                String regionBodyName = regionBody.substring(regionBody.indexOf(" ")).trim();

                // make the following changes:

                // Football
                // 1) Tσάμπιονς Λιγκ -> Ευρώπη
                // 2) Γιουρόπα Λιγκ -> Ευρώπη
                // 3) UEFA Nations League -> Ευρώπη
                // Basketball
                // 1) NBA -> Β.Αμερική
                // 2) Euroleague -> Ευρώπη

                switch (regionHead) {
                    // football
                    case "Tσάμπιονς Λιγκ":
                        regionHead = "Ευρώπη";
                        break;
                    case "Γιουρόπα Λιγκ":
                        regionHead = "Ευρώπη";
                        break;
                    case "UEFA Nations League":
                        regionHead = "Ευρώπη";
                        break;
                    // basketball
                    case "NBA":
                        regionHead = "Β. Αμερική";
                        break;
                    case "Euroleague":
                        regionHead = "Ευρώπη";
                        break;
                }

                // we check if the event name contains one of the words "Μακροχρόνια", "Νικητής", "Ειδικά στοιχήματα"
                // if it does not contains we add the event to database
                if (!(     regionBodyName.contains("Ενισχυμένη")
                        || regionBodyName.contains("Combi")
                        || regionBodyName.contains("Ειδικά")
                        || regionBodyName.contains("Προκριματικά")
                        // Basketball
                        || regionBodyName.contains("Πρόκριση")
                        || regionBodyName.contains("Είσοδος")
                        || regionBodyName.contains("Τερματισμός")
                        || regionBodyName.contains("τελικό")
                        || regionBodyName.contains("Σκορ"))
                        ) {

                    // System.out.println("regionBodyName |"+regionBodyName+"|");

                    // create the formatted name (event name)
                    // Αγγλία - Premier League, Αγγλία - Championship, Αγγλία - EFL Cup, ...
                    eventName = regionHead + " - " + regionBodyName;

                    // System.out.println("Event name: "+eventName);

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
        String cssPathForEventLinks = "#nav-special-leagues ul.sports-links.hide ul.sports-links.hide li a, " +
                                      "#nav-top-list ul.sports-links.hide li a, " +
                                      "#nav-more-list ul.sports-links.hide li a";
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
            if (!(     checkForLongTermBets.contains("Ενισχυμένη")
                    || checkForLongTermBets.contains("Combi")
                    || checkForLongTermBets.contains("Ειδικά")
                    || checkForLongTermBets.contains("Προκριματικά")
                    // Basketball
                    || checkForLongTermBets.contains("Πρόκριση")
                    || checkForLongTermBets.contains("Είσοδος")
                    || checkForLongTermBets.contains("Τερματισμός")
                    || checkForLongTermBets.contains("τελικό")
                    || checkForLongTermBets.contains("Σκορ"))
                    ) {

                // System.out.println("Event link (Inside SportingbetParser): "+eventLink);

                /*
                ONLY
                FOR
                DEBUGGING
                */

                // if(eventLink.contains("I-liga-Poland-17396")) {
                eventLinks.add(eventLink);
                // }

            }

            // if (eventLinks.size() == 8) {
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

    public void fetchGameLinks(Document doc) {

        // clear the list. The list must contain only the game links of the current event
        gameLinks.clear();

        // links
        String cssPathForGameLinks = ".mb-event-details-buttons__button.mb-event-details-buttons__button--more-markets a.mb-event-details-buttons__button-link";
        Elements gameLinksAsElement = doc.select(cssPathForGameLinks);

        for (Element link: gameLinksAsElement) {

            // link of game
            String gameLink = link.absUrl("href");

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

    @Override
    public void fetchGameData(Document doc) {

        // data for creating game

        // Game constructor:
        // (int gameCode, String opponents, LocalDate date, LocalTime time)

        // match code
        // sportingbet does not support match code
        String cssPathForGameCode = "";
        int gameCode = 0;

        // opponents
        String cssPathForOpponents = ".event-block__event-name";
        Elements opponentsAsElement = doc.select(cssPathForOpponents);

        // date and time
        String cssPathForDateAndTime = ".event-block__start-date";
        Elements dateAsElement = doc.select(cssPathForDateAndTime);

        // data to check the database

        // event name
        String cssPathForEventName = ".event-block__league-link";
        Element eventNameAsElement = doc.select(cssPathForEventName).first();
        // event name has the format: Πρέμιερ λίγκ, Αγγλία
        String eventNameAsString = eventNameAsElement.text();
        // we need to convert it to:  Αγγλία, Πρέμιερ λίγκ
        // region (Αγγλία, Γαλλία, ...) is the last word of the string
        String region = eventNameAsString.substring(eventNameAsString.lastIndexOf(",")+1).trim();
        // event (Πρέμιερ Λίγκ, Κύπελλο Ελλάδας, ...) is the rest of the string
        String event = eventNameAsString.substring(0, eventNameAsString.lastIndexOf(",")+1).trim();
        // remove the last character (",")
        event = event.substring(0, event.length()-1);
        // event name formatted
        String eventNameFormatted = region + " - " + event;
        // eventName variable will be used in the next class (fetchBetData)
        eventName = eventNameFormatted;
        // System.out.println("eventNameFormatted |"+eventNameFormatted+"|");

        // sport
        String cssPathForSportName = "#nav-sport-name.sports-links__text";
        String sportName = doc.select(cssPathForSportName).text();
        Sport sportToCheck = sportRepository.getOneByNameAndBettor(sportName, bettor);

        // System.out.println("sportToCheck "+sportToCheck);

        // sport object to check if associated with the event
        Event eventToCheck = eventRepository.getOneByNameAndSport(eventName, sportToCheck);

        // list with new games
        // new game is an game that is not contained in the database (associated with "eventToCheck")
        List<Game> newGames = new ArrayList<>();

        // new game
        Game newGame;

        // name of opponents to check
        String opponentsToCheck;

        // date and time of the game
        LocalDate date;
        LocalTime time;

        // name of opponents
        opponentsToCheck = opponentsAsElement.first().text();
        // opponents variable will be used in the next class (fetchBetData)
        opponents = opponentsToCheck;

        // date and time as string
        // SportingParser sample date-time Format
        // 2/3/19, 2:30 μμ
        String dateAsString = dateAsElement.first().text();

        Locale locale = new Locale("el", "GR");

        DateTimeFormatter stringToDateFormatter = new DateTimeFormatterBuilder()
                .appendPattern("d/M/yyyy, h:mm a")
                .toFormatter(locale);

        LocalDateTime dateTime = LocalDateTime.parse(dateAsString, stringToDateFormatter);

        date = dateTime.toLocalDate();
        time = dateTime.toLocalTime();

        // System.out.println("Inside SportingbetParser (date)): " + date);
        // System.out.println("Inside SportingbetParser (time)): " + time);
        //
        // System.out.println("Inside SportingbetParser - opponents: " + opponents);
        // System.out.println("Inside SportingbetParser - event to check: " + eventToCheck);
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

        // home and away win
        // home win appears in the FIRST row, FIRST column
        String cssPathForHomeWin = "td.mb-option-button--3-way:nth-child(1) > button:nth-child(2) > div:nth-child(2)";
        // away win appears in the FIRST row, THIRD column
        String cssPathForAwayWin = "td.mb-option-button--3-way:nth-child(3) > button:nth-child(2) > div:nth-child(2)";
        // elements
        Element homeWinAsElement = doc.select(cssPathForHomeWin).first();
        Element awayWinAsElement = doc.select(cssPathForAwayWin).first();

        // sport of the event
        Sport sportOfEvent = sportRepository.getOneByNameAndBettor(sportName, bettor);

        // event of the bet
        Event event = eventRepository.getOneByNameAndSport(eventName, sportOfEvent);

        // get the game of the bet (by name and event)
        try {
            gameOfBet = gameRepository.getOneByOpponentsAndEvent(opponents, event);
        } catch (Exception e) {
            throw new RuntimeException("Duplicate game name: "+ opponents +" to event: "+ eventName);
        }

        // print the game of bet
        // System.out.println("gameOfBet: "+gameOfBet);

        if (homeWinAsElement != null) {

            // home win
            String homeWinAsString = homeWinAsElement.text();

            // Format bet data
            // convert String to BigDecimal (homeWin)
            homeWin = new BigDecimal(homeWinAsString);
        }

        if (awayWinAsElement != null) {

            // away win
            String awayWinAsString = awayWinAsElement.text();

            // Format bet data
            // convert String to BigDecimal (awayWin)
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
                break;
            case "Τένις":
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

        // get home and away team
        // so we can read the labels of the bets
        // eg: Ολυμπιακός/ΑΕΚ is the bet for Ημίχρονο/Τελικό bet (1/2)
        // split opponents name at "-"
        String[] opponentsAsArray = opponents.split("-");
        // home team is the first element
        String homeTeam = opponentsAsArray[0].trim();
        // away team is the second element
        String awayTeam = opponentsAsArray[1].trim();

        // System.out.println("homeTeam: "+homeTeam);
        // System.out.println("awayTeam: "+awayTeam);

        // CSS paths
        // Get every label and every odd (except 1X2)
        String cssPathForOdds =  "div > table > tbody > tr > td > button > div.mb-option-button__option-odds";
        String cssPathForLabel = "div > table > tbody > tr > td > button > div.mb-option-button__option-name.mb-option-button__option-name--odds-4,"+
                                 "div > table > tbody > tr > td > button > div.mb-option-button__option-name.mb-option-button__option-name--odds-5,"+
                                 "div > table > tbody > tr > td > button > div.mb-option-button__option-name.mb-option-button__option-name--odds-6";

        // elements
        Elements odds = doc.select(cssPathForOdds);
        Elements labels = doc.select(cssPathForLabel);

        // fetch football bets

        // 1) draw is the second element (i=1)
        String drawAsString = odds.get(1).text();

        // convert String to BigDecimal
        draw = new BigDecimal(drawAsString);

        // set draw
        footballBet.setDraw(draw);

        // 2) rest of bets (goals under/over, double bet, ...)
        // the first 3 elements (i=0, i=1, i=2) belongs to 1X2 labels and odds
        // we don't need those elements so we loop through from the fourth element (i=3) and over
        for (int i=3; i<labels.size(); i++) {

            String label = labels.get(i).text();
            String oddAsString = odds.get(i).text();

            // System.out.println(label+" - "+odd);

            // odd as BigDecimal for under/over bets
            underOverOdd = new BigDecimal(oddAsString);

            // 1X
            if (label.equals(homeTeam+" ή X")) {

                homeDraw = new BigDecimal(oddAsString);
                footballBet.setHomeDraw(homeDraw);

            // 2X
            } else if (label.equals("X ή "+awayTeam)) {

                awayDraw = new BigDecimal(oddAsString);
                footballBet.setAwayDraw(awayDraw);

            // 12
            } else if (label.equals(homeTeam+" ή "+awayTeam)) {

                homeAway = new BigDecimal(oddAsString);
                footballBet.setHomeAway(homeAway);

            // over 1,5
            } else if (label.equals("Over 0,5")) {

                footballBet.setOver0_5(underOverOdd);

            // over 1,5
            } else if (label.equals("Over 1,5")) {

                footballBet.setOver1_5(underOverOdd);

            // over 2,5
            } else if (label.equals("Over 2,5")) {

                footballBet.setOver2_5(underOverOdd);

            // over 3,5
            } else if (label.equals("Over 3,5")) {

                footballBet.setOver3_5(underOverOdd);

            // over 4,5
            } else if (label.equals("Over 4,5")) {

                footballBet.setOver4_5(underOverOdd);

            // over 5,5
            } else if (label.equals("Over 5,5")) {

                footballBet.setOver5_5(underOverOdd);

            // over 6,5
            } else if (label.equals("Over 6,5")) {

                footballBet.setOver6_5(underOverOdd);

            // over 7,5
            } else if (label.equals("Over 7,5")) {

                footballBet.setOver7_5(underOverOdd);

            // under 0,5
            } else if (label.equals("Under 0,5")) {

                footballBet.setUnder0_5(underOverOdd);

            // under 1,5
            } else if (label.equals("Under 1,5")) {

                footballBet.setUnder1_5(underOverOdd);

            // under 2,5
            } else if (label.equals("Under 2,5")) {

                footballBet.setUnder2_5(underOverOdd);

            // under 3,5
            } else if (label.equals("Under 3,5")) {

                footballBet.setUnder3_5(underOverOdd);

            // under 4,5
            } else if (label.equals("Under 4,5")) {

                footballBet.setUnder4_5(underOverOdd);

            // under 5,5
            } else if (label.equals("Under 5,5")) {

                footballBet.setUnder5_5(underOverOdd);

            // under 6,5
            } else if (label.equals("Under 6,5")) {

                footballBet.setUnder6_5(underOverOdd);

            // under 7,5
            } else if (label.equals("Under 7,5")) {

                footballBet.setUnder7_5(underOverOdd);

            // GG
            } else if (label.equals("Ναι")) {

                goalGoal = new BigDecimal(oddAsString);
                footballBet.setGoalGoal(goalGoal);

            // NG
            } else if (label.equals("Όχι")) {

                noGoal = new BigDecimal(oddAsString);
                footballBet.setNoGoal(noGoal);

            // half - full time score
            // 1/1
            } else if (label.equals(homeTeam+" / "+homeTeam)) {

                home_home = new BigDecimal(oddAsString);
                footballBet.setHome_Home(home_home);

            // 2/1
            } else if (label.equals(awayTeam+" / "+homeTeam)) {

                away_home = new BigDecimal(oddAsString);
                footballBet.setAway_Home(away_home);

            // X/X
            } else if (label.equals("X / X")) {

                draw_draw = new BigDecimal(oddAsString);
                footballBet.setDraw_Draw(draw_draw);

            // 1/2
            } else if (label.equals(homeTeam+" / "+awayTeam)) {

                home_away = new BigDecimal(oddAsString);
                footballBet.setHome_Away(home_away);

            // 2/2
            } else if (label.equals(awayTeam+" / "+awayTeam)) {

                away_away = new BigDecimal(oddAsString);
                footballBet.setAway_Away(away_away);

            // X/1
            } else if (label.equals("X / "+homeTeam)) {

                draw_home = new BigDecimal(oddAsString);
                footballBet.setDraw_Home(draw_home);

            // 1/X
            } else if (label.equals(homeTeam+" / X")) {

                home_draw = new BigDecimal(oddAsString);
                footballBet.setHome_Draw(home_draw);

            // 2/X
            } else if (label.equals(awayTeam+" / X")) {

                away_draw = new BigDecimal(oddAsString);
                footballBet.setAway_Draw(away_draw);

            // X/2
            } else if (label.equals("X / "+awayTeam)) {

                draw_away = new BigDecimal(oddAsString);
                footballBet.setDraw_Away(draw_away);

            }

        }

        // ALL data has been set to the football bet object

        // associate game with bets
        footballBet.setGame(gameOfBet);

        // save bet
        saveEntity(footballBet, betRepository);

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

    public SportingbetParser() {
    }

    // getters and setters

    public static String getBettorName() {
        return BETTOR_NAME;
    }

    public void setBettorName(String bettorName) {
        this.bettorName = bettorName;
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public Game getGameOfBet() {
        return gameOfBet;
    }

    public void setGameOfBet(Game gameOfBet) {
        this.gameOfBet = gameOfBet;
    }

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
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

    public static String getIndexPageUrl() {
        return INDEX_PAGE_URL;
    }

    public Bettor getBettor() {
        return bettor;
    }

    public void setBettor(Bettor bettor) {
        this.bettor = bettor;
    }

    @Override
    public String toString() {
        return "StoiximanParser{" +
                "BETTOR_NAME='" + BETTOR_NAME + '\'' +
                ", INDEX_PAGE_URL='" + INDEX_PAGE_URL + '\'' +
                '}';
    }
}
