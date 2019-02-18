package com.kavou.bettingCrawler.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="bet")
@Inheritance(strategy=InheritanceType.JOINED)
public class Bet {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="home_win")
    private BigDecimal homeWin;

    @Column(name="away_win")
    private BigDecimal awayWin;

    @JsonFormat(pattern="dd/MM/yyyy-HH:mm", timezone="Europe/Athens")
    @Column(name="created_at")
    private LocalDateTime createdAt;

    // associate game with bet
    @ManyToOne()
    @JoinColumn(name="game_id")
    private Game game;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Bet() {
    }

    public Bet(BigDecimal homeWin, BigDecimal awayWin) {
        this.homeWin = homeWin;
        this.awayWin = awayWin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "Bet{" +
                ", homeWin=" + homeWin +
                ", awayWin=" + awayWin +
                ", createdAt=" + createdAt +
                '}';
    }
}
