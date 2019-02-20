package com.kavou.bettingCrawler.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="game")
public class Game {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="game_id")
    private int id;

    @Column(name="game_code")
    private int gameCode;

    @Column(name="opponents")
    private String opponents;

    @JsonFormat(pattern="dd/MM/yyyy", timezone="Europe/Athens")
    @DateTimeFormat(pattern="dd/MM/yyyy")
    @Column(name="date")
    private LocalDate date;

    @JsonFormat(pattern="HH:mm", timezone="Europe/Athens")
    @DateTimeFormat(pattern="HH:mm")
    @Column(name="time")
    private LocalTime time;

    // associate event with game
    @ManyToOne()
    @JoinColumn(name="event_id")
    private Event event;

    // associate game with bet
    @OneToMany(cascade=CascadeType.ALL, mappedBy="game")
    private List<Bet> bets;

    public Game() {
    }

    public Game(int gameCode, String opponents, LocalDate date, LocalTime time) {
        this.gameCode = gameCode;
        this.opponents = opponents;
        this.date = date;
        this.time = time;
    }

    public Game(int id, int gameCode, String opponents, LocalDate date, LocalTime time) {
        this.id = id;
        this.gameCode = gameCode;
        this.opponents = opponents;
        this.date = date;
        this.time = time;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatchCode() {
        return gameCode;
    }

    public void setMatchCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public String getOpponents() {
        return opponents;
    }

    public void setOpponents(String opponents) {
        this.opponents = opponents;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public List<Bet> getBets() {
        return bets;
    }

    public void setBets(List<Bet> bets) {
        this.bets = bets;
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameCode=" + gameCode +
                ", opponents='" + opponents + '\'' +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
