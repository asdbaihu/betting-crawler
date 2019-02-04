package com.kavou.bettingCrawler.web.api.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="game")
public class Game {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="sport")
    private String sport;

    @Column(name="bettor")
    private String bettor;

    @Column(name="match_code")
    private int matchCode;

    @Column(name="event")
    private String event;

    @Column(name="opponents")
    private String opponents;

    @JsonFormat(pattern="dd/MM/yyyy", timezone="Europe/Athens")
    @Column(name="date")
    @Temporal(TemporalType.DATE)
    private Date date;

    @JsonFormat(pattern="HH:mm", timezone="Europe/Athens")
    @Column(name="time")
    @Temporal(TemporalType.TIME)
    private Date time;

    @JsonFormat(pattern="dd/MM/yyyy-HH:mm", timezone="Europe/Athens")
    @Column(name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    // column name of bet table (in Database, not in Class)
    // associate Game with Bet
    @JoinColumn(name="game_id")
    private List<Bet> bets;

    public Game() {
    }

    public Game(String sport, String bettor, int matchCode, String event, String opponents, Date date, Date time) {
        this.sport = sport;
        this.bettor = bettor;
        this.matchCode = matchCode;
        this.event = event;
        this.opponents = opponents;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getBettor() {
        return bettor;
    }

    public void setBettor(String bettor) {
        this.bettor = bettor;
    }

    public int getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(int matchCode) {
        this.matchCode = matchCode;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getOpponents() {
        return opponents;
    }

    public void setOpponents(String opponents) {
        this.opponents = opponents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Bet> getBets() {
        return bets;
    }

    public void setBets(List<Bet> bets) {
        this.bets = bets;
    }

    @Override
    public String toString() {
        return "Game{" +
                "sport='" + sport + '\'' +
                ", bettor='" + bettor + '\'' +
                ", matchCode=" + matchCode +
                ", event='" + event + '\'' +
                ", opponents='" + opponents + '\'' +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
