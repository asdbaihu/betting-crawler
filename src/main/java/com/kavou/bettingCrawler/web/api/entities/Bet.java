package com.kavou.bettingCrawler.web.api.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="bet")
@Inheritance(strategy=InheritanceType.JOINED)
public class Bet {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="sport")
    private String sport;

    @Column(name="bettor")
    private String bettor;

    @Column(name="home_win")
    private BigDecimal homeWin;

    @Column(name="away_win")
    private BigDecimal awayWin;

    @JsonFormat(pattern="dd/MM/yyyy-HH:mm", timezone="Europe/Athens")
    @Column(name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    public Bet() {
    }

    public Bet(String sport, String bettor, BigDecimal homeWin, BigDecimal awayWin) {
        this.sport = sport;
        this.bettor = bettor;
        this.homeWin = homeWin;
        this.awayWin = awayWin;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Bet{" +
                "sport='" + sport + '\'' +
                ", bettor='" + bettor + '\'' +
                ", homeWin=" + homeWin +
                ", awayWin=" + awayWin +
                ", createdAt=" + createdAt +
                '}';
    }
}
